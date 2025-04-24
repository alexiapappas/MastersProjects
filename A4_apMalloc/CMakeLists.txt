cmake_minimum_required(VERSION 3.30)
project(A4_apMalloc)

set(CMAKE_CXX_STANDARD 20)

add_executable(A4_apMalloc main.cpp
        LPHT.cpp
        LPHT.h
        apMalloc.cpp
        apMalloc.h)
