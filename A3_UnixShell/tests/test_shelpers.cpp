//
// Created by Alexia Pappas on 2/9/25.
//

#include <iostream>
#include <vector>
#include <cassert>
#include <unistd.h>

#include "../src/shelpers.h"

std::vector<Command> getCommands(const std::vector<Command>& tokens);

void testGetCommands() {
    // Test: Simple command
    {
        std::vector<std::string> input = {"ls"};
        auto commands = getCommands(input);
        assert(commands.size() == 1);
        assert(commands[0].execName == "ls");
    }

    // Test: Command with arguments
    {
        std::vector<std::string> input = {"echo", "hello", "world"};
        auto commands = getCommands(input);
        assert(commands.size() == 1);
        assert(commands[0].argv.size() == 4); // "echo", "hello", "world", nullptr
    }

    // Test: Input redirection
    {
        std::vector<std::string> input = {"cat", "<", "file.txt"};
        auto commands = getCommands(input);
        assert(commands.size() == 1);
        assert(commands[0].execName == "cat");
        assert(commands[0].inputFd != STDIN_FILENO); // Should have a file descriptor
    }

    // Test: Output redirection
    {
        std::vector<std::string> input = {"ls", ">", "output.txt"};
        auto commands = getCommands(input);
        assert(commands.size() == 1);
        assert(commands[0].execName == "ls");
        assert(commands[0].outputFd != STDOUT_FILENO); // Should have a file descriptor
    }

    // Test: Background command
    {
        std::vector<std::string> input = {"sleep", "10", "&"};
        auto commands = getCommands(input);
        assert(commands.size() == 1);
        assert(commands[0].background == true);
    }

    // Test: Invalid case (missing filename after >)
    {
        std::vector<std::string> input = {"echo", "hello", ">"};
        auto commands = getCommands(input);
        assert(commands.empty()); // Should fail due to syntax error
    }

    std::cout << "All tests passed!" << std::endl;
}