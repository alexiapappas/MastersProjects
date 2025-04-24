#include <iostream>
#include <chrono>
#include "apMalloc.h"

using namespace std::chrono;


int main() {
    std::cout << "Testing MyMalloc\n";

    // Allocate a large number of small objects
    std::vector<void*> small_allocs;
    for (int i = 0; i < 30000; i++) {
        void* ptr = malloc(1);
        if (!ptr) {
            std::cerr << "Allocation failed!\n";
            return 1;
        }
        small_allocs.push_back(ptr);
    }

    // Manipulate data
    for (auto ptr : small_allocs) {
        memset(ptr, 42, 64);  // Fill with arbitrary value
    }

    // Free small objects
    for (auto ptr : small_allocs) {
        free(ptr);
    }
    small_allocs.clear();

    // Allocate and test large objects
    std::vector<void*> large_allocs;
    for (int i = 0; i < 1000; i++) {
        void* ptr = malloc(1024 * 1024); // 1MB allocations
        if (!ptr) {
            std::cerr << "Large allocation failed!\n";
            return 1;
        }
        large_allocs.push_back(ptr);
    }

    // Free large objects
    for (auto ptr : large_allocs) {
        free(ptr);
    }
    large_allocs.clear();

    // Benchmarking malloc/free
    // auto start = std::chrono::high_resolution_clock::now();
    // for (int i = 0; i < 100000; i++) {
    //     void* ptr = malloc(128);
    //     free(ptr);
    // }
    // auto end = std::chrono::high_resolution_clock::now();
    // std::cout << "Custom malloc/free took "
    //           << std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count()
    //           << " ms\n";

    constexpr size_t numAllocations = 10000;
    constexpr size_t allocSize = 128;


    // Benchmark standard malloc
    auto start1 = high_resolution_clock::now();
    for (size_t i = 0; i < numAllocations; i++) {
        void* ptr = std::malloc(allocSize);
        std::free(ptr);
    }
    auto end1 = high_resolution_clock::now();
    std::cout << "Standard malloc/free time: "
         << duration_cast<milliseconds>(end1 - start1).count()
         << " ms" << std::endl;

    // Benchmark MyAllocator's malloc
    auto start2 = high_resolution_clock::now();
    for (size_t i = 0; i < numAllocations; i++) {
        void* ptr = apAllocator::malloc(allocSize);
        apAllocator::free(ptr);
    }
    auto end2 = high_resolution_clock::now();
    std::cout << "MyAllocator malloc/free time: "
         << duration_cast<milliseconds>(end2 - start2).count()
         << " ms" << std::endl;


    return 0;
}