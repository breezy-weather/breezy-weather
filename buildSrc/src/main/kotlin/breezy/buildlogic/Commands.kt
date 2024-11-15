package breezy.buildlogic

import org.gradle.api.Project
import java.io.ByteArrayOutputStream

// Git is needed in your system PATH for these commands to work.
// If it's not installed, you can return a random value as a workaround
fun Project.getCommitCount(): String {
    return runCommand("git rev-list --count HEAD")
    // return "1"
}

fun Project.getGitSha(): String {
    return runCommand("git rev-parse --short=8 HEAD")
    // return "1"
}

fun Project.runCommand(command: String): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = command.split(" ")
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}
