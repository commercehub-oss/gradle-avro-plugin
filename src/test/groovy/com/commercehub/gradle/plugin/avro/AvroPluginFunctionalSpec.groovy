/*
 * Copyright © 2015-2017 Commerce Technologies, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.commercehub.gradle.plugin.avro

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AvroPluginFunctionalSpec extends FunctionalSpec {
    def "setup"() {
        applyAvroPlugin()
        addAvroDependency()
    }

    def "can generate and compile java files from json schema"() {
        given:
        copyResource("user.avsc", avroDir)

        when:
        def result = run()

        then:
        taskInfoAbsent || result.task(":generateAvroJava").outcome == SUCCESS
        taskInfoAbsent || result.task(":compileJava").outcome == SUCCESS
        projectFile(buildOutputClassPath("example/avro/User.class")).file
    }

    def "can generate and compile java files from json protocol"() {
        given:
        addAvroIpcDependency()
        copyResource("mail.avpr", avroDir)

        when:
        def result = run()

        then:
        taskInfoAbsent || result.task(":generateAvroJava").outcome == SUCCESS
        taskInfoAbsent || result.task(":compileJava").outcome == SUCCESS
        projectFile(buildOutputClassPath("org/apache/avro/test/Mail.class")).file
        projectFile(buildOutputClassPath("org/apache/avro/test/Message.class")).file
    }

    def "can generate and compile java files from IDL"() {
        given:
        copyResource("interop.avdl", avroDir)

        when:
        def result = run()

        then:
        taskInfoAbsent || result.task(":generateAvroProtocol").outcome == SUCCESS
        taskInfoAbsent || result.task(":generateAvroJava").outcome == SUCCESS
        taskInfoAbsent || result.task(":compileJava").outcome == SUCCESS
        projectFile(buildOutputClassPath("org/apache/avro/Foo.class")).file
        projectFile(buildOutputClassPath("org/apache/avro/Interop.class")).file
        projectFile(buildOutputClassPath("org/apache/avro/Kind.class")).file
        projectFile(buildOutputClassPath("org/apache/avro/MD5.class")).file
        projectFile(buildOutputClassPath("org/apache/avro/Node.class")).file
    }

    def "supports json schema files in subdirectories"() {
        given:
        copyResource("user.avsc", avroSubDir)

        when:
        def result = run()

        then:
        taskInfoAbsent || result.task(":generateAvroJava").outcome == SUCCESS
        taskInfoAbsent || result.task(":compileJava").outcome == SUCCESS
        projectFile(buildOutputClassPath("example/avro/User.class")).file
    }

    def "supports json protocol files in subdirectories"() {
        given:
        addAvroIpcDependency()
        copyResource("mail.avpr", avroSubDir)

        when:
        def result = run()

        then:
        taskInfoAbsent || result.task(":generateAvroJava").outcome == SUCCESS
        taskInfoAbsent || result.task(":compileJava").outcome == SUCCESS
        projectFile(buildOutputClassPath("org/apache/avro/test/Mail.class")).file
        projectFile(buildOutputClassPath("org/apache/avro/test/Message.class")).file
    }

    def "supports IDL files in subdirectories"() {
        given:
        copyResource("interop.avdl", avroSubDir)

        when:
        def result = run()

        then:
        taskInfoAbsent || result.task(":generateAvroProtocol").outcome == SUCCESS
        taskInfoAbsent || result.task(":generateAvroJava").outcome == SUCCESS
        taskInfoAbsent || result.task(":compileJava").outcome == SUCCESS
        projectFile(buildOutputClassPath("org/apache/avro/Foo.class")).file
        projectFile(buildOutputClassPath("org/apache/avro/Interop.class")).file
        projectFile(buildOutputClassPath("org/apache/avro/Kind.class")).file
        projectFile(buildOutputClassPath("org/apache/avro/MD5.class")).file
        projectFile(buildOutputClassPath("org/apache/avro/Node.class")).file
    }

    def "gives a meaningful error message when presented a malformed schema file"() {
        given:
        copyResource("enumMalformed.avsc", avroDir)
        def errorFilePath = new File("src/main/avro/enumMalformed.avsc").path

        when:
        def result = runAndFail()

        then:
        taskInfoAbsent || result.task(":generateAvroJava").outcome == FAILED
        result.output.contains("> Could not compile schema definition files:")
        result.output.contains("* $errorFilePath: \"enum\" is not a defined name. The type of the \"gender\" " +
                "field must be a defined name or a {\"type\": ...} expression.")
    }

    def "avro plugin correctly uses task configuration avoidance"() {
        given:
            buildFile << """
                def configuredTasks = []
                tasks.configureEach {
                    configuredTasks << it
                }
                
                gradle.buildFinished {
                    def configuredTaskPaths = configuredTasks*.path
                    
                    assert configuredTaskPaths == [':help']
                }
            """
        when:
        def result = run("help")

        then:
        assert result
    }
}
