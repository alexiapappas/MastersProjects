//
// Created by Alexia Pappas on 2/23/25.
//

#include <cassert>
#include <cstdio>
#include <iostream>
#include <sys/mman.h>
#include "apMalloc.h"
#include "LPHT.h"

using namespace std;

constexpr size_t pageSize = 4096;

apMalloc* apMalloc::create() {
    apMalloc* instance = static_cast<apMalloc *>(allocatePage(sizeof(apMalloc)));
    instance->allocations = *HashTable::create(11);
    return instance;
}

void* apMalloc::allocate(size_t bytesToAllocate) {
    size_t alignedSize = (bytesToAllocate + pageSize - 1) & ~(pageSize - 1); // Round up to the nearest multiple of page size
    void* pointer = mmap(nullptr, alignedSize, PROT_READ | PROT_WRITE, MAP_ANONYMOUS | MAP_PRIVATE, -1, 0);
    if (pointer == MAP_FAILED) {
        perror("Failed to allocate memory");
        return nullptr;
    }

    allocations.insertKey(pointer, alignedSize);
    return pointer;
}

void apMalloc::deallocate(void* pointer) {
    size_t size = allocations.removeKey(pointer);
    if (size > 0) {
        munmap(pointer, size);
    } else {
        std::cerr << "Failed to deallocate memory" << std::endl;
    }
}

namespace apAllocator {
    apMalloc* globalAllocator = apMalloc::create();

    // Overloaded malloc
    void* malloc (size_t size) {
        return globalAllocator->allocate(size);
    }

    // Overloaded free
    void free(void* ptr) {
        globalAllocator->deallocate(ptr);
    }
}