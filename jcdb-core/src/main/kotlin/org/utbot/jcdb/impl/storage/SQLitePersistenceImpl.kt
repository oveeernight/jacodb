package org.utbot.jcdb.impl.storage

import mu.KLogging
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import org.utbot.jcdb.api.ClassSource
import org.utbot.jcdb.api.JCDBPersistence
import org.utbot.jcdb.api.JcByteCodeLocation
import org.utbot.jcdb.api.JcClasspath
import org.utbot.jcdb.api.RegisteredLocation
import org.utbot.jcdb.impl.FeaturesRegistry
import org.utbot.jcdb.impl.JcInternalSignal
import org.utbot.jcdb.impl.fs.ClassSourceImpl
import org.utbot.jcdb.impl.fs.JavaRuntime
import org.utbot.jcdb.impl.fs.asByteCodeLocation
import org.utbot.jcdb.impl.fs.info
import org.utbot.jcdb.impl.storage.jooq.tables.references.BYTECODELOCATIONS
import org.utbot.jcdb.impl.storage.jooq.tables.references.CLASSES
import org.utbot.jcdb.impl.storage.jooq.tables.references.SYMBOLS
import java.io.Closeable
import java.io.File
import java.sql.Connection
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import javax.sql.DataSource
import kotlin.concurrent.withLock

class SQLitePersistenceImpl(
    private val javaRuntime: JavaRuntime,
    private val featuresRegistry: FeaturesRegistry,
    location: String? = null,
    private val clearOnStart: Boolean
) : JCDBPersistence, Closeable {

    companion object : KLogging()

    private val lock = ReentrantLock()

    private val dataSource: DataSource
    private var keepAliveConnection: Connection? = null
    private val persistenceService = PersistenceService(this)
    val jooq: DSLContext

    init {
        val config = SQLiteConfig().also {
            it.setSynchronous(SQLiteConfig.SynchronousMode.OFF)
            it.setJournalMode(SQLiteConfig.JournalMode.OFF)
            it.setPageSize(32_768)
            it.setCacheSize(-8_000)
        }
        if (location == null) {
            val url = "jdbc:sqlite:file:jcdb-${UUID.randomUUID()}?mode=memory&cache=shared&rewriteBatchedStatements=true"
            dataSource = SQLiteDataSource(config).also {
                it.url = url
            }
            keepAliveConnection = dataSource.connection //DriverManager.getConnection(url)
        } else {
            val url = "jdbc:sqlite:file:$location?rewriteBatchedStatements=true&useServerPrepStmts=false"
            dataSource = SQLiteDataSource(config).also {
                it.url = url
            }
        }
        jooq = DSL.using(dataSource, SQLDialect.SQLITE)
        write {
            if (clearOnStart) {
                jooq.executeQueriesFrom("jcdb-drop-schema.sql")
            }
            jooq.executeQueriesFrom("jcdb-create-schema.sql")
        }
    }

    override fun setup() {
        write {
            featuresRegistry.broadcast(JcInternalSignal.BeforeIndexing(clearOnStart))
        }
        persistenceService.setup()
    }

    override val locations: List<JcByteCodeLocation>
        get() {
            return jooq.selectFrom(BYTECODELOCATIONS).fetch().mapNotNull {
                try {
                    File(it.path!!).asByteCodeLocation(javaRuntime.version, isRuntime = it.runtime!!)
                } catch (e: Exception) {
                    null
                }
            }.toList()
        }

    override fun newSymbolInterner() = persistenceService.newSymbolInterner()

    override fun <T> write(action: (DSLContext) -> T): T {
        return lock.withLock {
            action(jooq)
        }
    }

    override fun <T> read(action: (DSLContext) -> T): T {
        return action(jooq)
    }

    override fun findSymbolId(symbol: String): Long? {
        return persistenceService.findSymbolId(symbol)
    }

    override fun findSymbolName(symbol: Long): String {
        return persistenceService.findSymbolName(symbol)
    }

    override fun findClassSourceByName(
        cp: JcClasspath,
        locations: List<RegisteredLocation>,
        fullName: String
    ): ClassSource? {
        val ids = locations.map { it.id }
        val symbolId = jooq.select(SYMBOLS.ID).from(SYMBOLS)
            .where(SYMBOLS.NAME.eq(fullName))
            .fetchAny()?.component1() ?: return null
        val found = jooq.select(CLASSES.LOCATION_ID, CLASSES.BYTECODE).from(CLASSES)
            .where(CLASSES.NAME.eq(symbolId).and(CLASSES.LOCATION_ID.`in`(ids)))
            .fetchAny() ?: return null
        val locationId = found.component1()!!
        val byteCode = found.component2()!!
        return ClassSourceImpl(
            location = locations.first { it.id == locationId },
            className = fullName,
            byteCode = byteCode
        )
    }

    override fun findClassSources(location: RegisteredLocation): List<ClassSource> {
        val classes = jooq.select(CLASSES.LOCATION_ID, CLASSES.BYTECODE, SYMBOLS.NAME).from(CLASSES)
            .join(SYMBOLS).on(CLASSES.NAME.eq(SYMBOLS.ID))
            .where(CLASSES.LOCATION_ID.eq(location.id))
            .fetch()
        return classes.map {
            ClassSourceImpl(
                location = location,
                className = it.component3()!!,
                byteCode = it.component2()!!
            )
        }
    }

    override fun persist(location: RegisteredLocation, classes: List<ClassSource>) {
        val allClasses = classes.map { it.info }
        persistenceService.persist(location, allClasses)
    }

    override fun close() {
        try {
            keepAliveConnection?.close()
        } catch (e: Exception) {
            // ignore
        }
    }

}