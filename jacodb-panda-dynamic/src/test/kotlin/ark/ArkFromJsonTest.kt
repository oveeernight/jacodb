/*
 *  Copyright 2022 UnitTestBot contributors (utbot.org)
 * <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ark

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jacodb.panda.dynamic.ark.dto.ArkFileDto
import org.jacodb.panda.dynamic.ark.dto.ConstantDto
import org.jacodb.panda.dynamic.ark.dto.FieldDto
import org.jacodb.panda.dynamic.ark.dto.LocalDto
import org.jacodb.panda.dynamic.ark.dto.convertToArkValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.jacodb.panda.dynamic.ark.base.AnyType as ArkAnyType
import org.jacodb.panda.dynamic.ark.base.Local as ArkLocal

class ArkFromJsonTest {
    private val json = Json {
        // classDiscriminator = "_"
        prettyPrint = true
    }

    @Test
    fun testLoadArkFileFromJson() {
        val path = "basic.ts.json"
        val stream = object {}::class.java.getResourceAsStream("/$path")
            ?: error("Resource not found: $path")
        val arkDto = ArkFileDto.loadFromJson(stream)
        println(arkDto)
    }

    @Test
    fun testLoadValueFromJson() {
        val jsonString = """
            {
                "name": "x",
                "type": "any"
            }
        """.trimIndent()
        val valueDto = Json.decodeFromString<LocalDto>(jsonString)
        Assertions.assertEquals(LocalDto("x", "any"), valueDto)
        val value = convertToArkValue(valueDto)
        Assertions.assertEquals(ArkLocal("x", ArkAnyType), value)
    }

    @Test
    fun testLoadFieldFromJson() {
        val field = FieldDto(
            name = "x",
            modifiers = emptyList(),
            type = "number",
            questionToken = false,
            initializer = ConstantDto("0", "number"),
        )
        println("field = $field")

        val jsonString = json.encodeToString(field)
        println("json: $jsonString")

        val fieldDto = json.decodeFromString<FieldDto>(jsonString)
        println("fieldDto = $fieldDto")
        Assertions.assertEquals(field, fieldDto)
    }
}
