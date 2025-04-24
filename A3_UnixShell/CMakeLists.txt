cmake_minimum_required(VERSION 3.30)
project(A3_UnixShell)

set(CMAKE_CXX_STANDARD 20)

include_directories(src)

add_executable(A3_UnixShell
        src/main.cpp
        src/shelpers.cpp
        src/shelpers.h
        tests/test_shelpers.cpp
)
