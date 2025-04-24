//
// Created by Alexia Pappas on 2/23/25.
//

#ifndef APMALLOC_H
#define APMALLOC_H

#include "LPHT.h"

class apMalloc {
public:
    HashTable allocations;

    static apMalloc* create();

    void* allocate(size_t bytesToAllocate);

    void deallocate(void* pointer);
};

namespace apAllocator {
    extern apMalloc* globalAllocator;

    void* malloc(size_t size);

    void free(void* pointer);
}

#endif //APMALLOC_H