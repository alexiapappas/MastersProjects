#include <iostream>
#include <map>
#include <string>
#include <unistd.h>
#include "shelpers.h"

// A map to keep track of background processes, key is the PID, value is the Command
std::map<pid_t, Command> backgroundCmds;

/**
 * Checks if any background commands have completed.
 * - Calls waitpid() with WNOHANG to check if a process has finished without blocking.
 * - If a process has finished, it prints a message and removes it from tracking.
 */
void checkBGCmds() {
    std::vector<pid_t> pids;
    for (const auto& pair : backgroundCmds) {
        pid_t childPid = waitpid(pair.first, nullptr, WNOHANG); // Check process status
        if (childPid == pair.first) { // If the process has completed
            std::cout << "Background process: " << pair.second.execName << " has finished." << std::endl;
            pids.push_back(childPid); // Add to list to be removed
        }

    }

    // Remove completed processes from the map
    for (const auto& pid : pids) {
        backgroundCmds.erase(pid);
    }
}

int main() {
    std::string command;
    std::cout << "x "; // My shell prompt

    while (getline(std::cin, command)) {
        checkBGCmds(); // Check if any background processes have finished
        std::cout << "x ";
        if (command == "exit") {
            break;
        }

        // Tokenize the command input and parse it into Command structs
        std::vector<std::string> commandNames = tokenize(command);
        std::vector<Command> commandVector = getCommands(commandNames);

        for (int i = 0; i < commandVector.size(); i++) {
            Command cmd = commandVector[i];

            if (cmd.execName == "cd") {
                if (cmd.argv.size() < 3) {
                    chdir(getenv("HOME")); // Change to home directory if no argument gien
                } else {
                    const char* path = cmd.argv[1];
                    chdir( path );
                }
                continue;
            }

            pid_t pid = fork();

            if (pid < 0) {
                perror("Fork failure");
                exit(1);
            }
            if (pid == 0) {
                // Child Process: close unused pipes in all other commands to prevent deadlocks
                for (int j = 0; j < commandVector.size(); j++) {
                    if (j != i) {
                        close(commandVector[j].inputFd);
                        close(commandVector[j].outputFd);
                    }
                }

                dupProcess(cmd); // Redirect input/output if necessary

                execvp(cmd.execName.c_str(), const_cast<char * const*>(cmd.argv.data()));
                perror("Execvp failure");
                exit(1);
            } else {
                // Parent process
                int parentPID;
                if (!cmd.background) {
                    waitpid(pid, &parentPID, 0); // Wait for foreground processes
                } else {
                    backgroundCmds[pid] = cmd; // Store background processes in map
                }

                // Close file descriptors to prevent resource leaks
                if (cmd.inputFd != STDIN_FILENO) {
                    close(cmd.inputFd);
                }

                if (cmd.outputFd != STDOUT_FILENO) {
                    close(cmd.outputFd);
                }
            }
        }
    }
    return 0;
}