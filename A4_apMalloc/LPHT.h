//
// Created by Alexia Pappas on 3/4/25.
//

#ifndef LPHT_H
#define LPHT_H

#include <cstddef>

void* allocatePage(size_t numBytes);

class HashTable {
    // Will hold key-value pairs --> aka the pointer to the allocated memory and its size
    struct HashEntry {
        void* pointerKey_;
        size_t size_;
    };

    HashEntry* table;
    size_t capacity;
    size_t entriesSize;

public:
    size_t hash(void* pointer);

    void growHashTable();

    void* find(void* pointer);

    static HashTable *create(size_t capacity);

    ~HashTable();

    void insertKey(void* key, size_t size);

    size_t removeKey(void* key);
};

#endif //LPHT_H