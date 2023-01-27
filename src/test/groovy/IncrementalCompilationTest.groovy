import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files

class IncrementalCompilationTest extends Specification {
    @TempDir
    File testProjectDir
    File wrongDirectorySourceFile
    File rightDirectorySourceFile
    File classFile

    def setup() {
        new File(testProjectDir, 'settings.gradle') << """
            rootProject.name = 'incremental-build-test'
        """

        new File(testProjectDir, 'build.gradle') << """
            plugins {
                id 'java'
            }
        """

        wrongDirectorySourceFile = new File(testProjectDir, 'src/main/java/wrong/Main.java')
        rightDirectorySourceFile = new File(testProjectDir, 'src/main/java/right/Main.java')
        classFile = new File(testProjectDir, 'build/classes/java/main/right/Main.class')

        wrongDirectorySourceFile.getParentFile().mkdirs()
        rightDirectorySourceFile.getParentFile().mkdirs()

        wrongDirectorySourceFile << """
            package right;
            public class Main {
            }
        """
    }

    def "full compilation with source file in wrong directory creates expected class file"() {
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('compileJava')
                .build()

        then:
        classFile.exists()
    }

    def "incremental compilation with source file moved to right directory recreates/keeps expected class file"() {
        setup:
        GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('compileJava')
                .build()

        Files.move(wrongDirectorySourceFile.toPath(), rightDirectorySourceFile.toPath())

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('compileJava')
                .build()

        then:
        classFile.exists()
    }
}
