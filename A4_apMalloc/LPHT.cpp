//
// Created by Alexia Pappas on 2/23/25.
//

#include <cassert>
#include <cstdint>
#include <cstdlib>
#include <cstring>
#include <iostream>
#include <sys/mman.h>
#include "LPHT.h"


constexpr size_t initialTableSize = 1024;
constexpr size_t pageSize = 4096;
constexpr uintptr_t tombstone = -1;


void* allocatePage(size_t numBytes) {
    return mmap(nullptr, numBytes, PROT_READ | PROT_WRITE, MAP_ANONYMOUS | MAP_PRIVATE, -1, 0);
}


HashTable *HashTable::create(size_t capacity) {
    auto* hashTable = static_cast<HashTable*>(allocatePage(sizeof(HashTable)));

    if (hashTable->table == MAP_FAILED) {
        perror("mmap failed: HashTable entries allocation");
        exit(1);
    }

    hashTable->capacity = capacity;
    hashTable->table = static_cast<HashEntry *>(mmap(nullptr, capacity * sizeof(HashEntry), PROT_READ | PROT_WRITE, MAP_ANONYMOUS | MAP_PRIVATE, -1, 0));

    std::memset(hashTable->table, 0, capacity * sizeof(HashEntry));

    return hashTable;
}


// Destructor
HashTable::~HashTable() {
    munmap(table, capacity * sizeof(HashEntry));
}


/*  Hash function using right-shift to avoid hashing based on the lower bits --> since memory is allocated in pages, the lower 12 bits of a pointer address remain
 *  constant for any address within the same page. Ensures hashing based on pages rather than byte addresses */
size_t HashTable::hash(void *pointer) {
    return (reinterpret_cast<uintptr_t>(pointer) >> 12);
}


void HashTable::growHashTable() {
    size_t newCapacity = capacity * 2;

    HashEntry *newTable = reinterpret_cast<HashEntry *>(mmap(nullptr, newCapacity * sizeof(HashEntry), PROT_READ | PROT_WRITE, MAP_ANONYMOUS | MAP_PRIVATE, -1, 0));

    if (newTable == MAP_FAILED) {
        perror("mmap failed: Unable to allocate memory for hash table growth");
        exit(1);
    }

    /* Initialize newly allocated hash table memory:
     * newTable is a pointer to the newly allocated hash table
     * 0 sets all bytes in the allocated memory to 0, ensures key is nullptr and size is 0
     * length calculates the total number of bytes to be set to zero */
    std::memset(newTable, 0, newCapacity * sizeof(HashEntry));

    // Rehash existing entries into the new table
    for (size_t i = 0; i < capacity; i++) {
        // if pointerKey_ is not null and not a tombstone, the entry is valid and needs to be moved
        if (table[i].pointerKey_ && table[i].pointerKey_ != reinterpret_cast<void *>(tombstone)) {
            size_t newIndex = (reinterpret_cast<uintptr_t>(table[i].pointerKey_) >> 12) % newCapacity;
            while (newTable[newIndex].pointerKey_) {
                // if computed newIndex is already occupied, probe forward using linear probing, keep incrementing newIndex until an empty slot is found
                newIndex = (newIndex + 1) % newCapacity;
            }
            // Once an empty slot is found copy the entry into the new table
            newTable[newIndex] = table[i];
        }
    }

    // Free old table memory and update table reference
    munmap(table, capacity * sizeof(HashEntry));
    /* A lazy delete is when the data at that address is not deleted, the pointer is simply set to null. The data only gets deleted when a new pointer
    is set to that address and new data needs to be added in */

    table = newTable;
    capacity = newCapacity;
}


void* HashTable::find(void* pointer) {
    if (pointer == nullptr) {
        std::cerr << "Error: HashTable instance is null!" << std::endl;
        return nullptr;
    }

    size_t index = hash(pointer) % capacity;
    if (index >= capacity) {  // This should never happen
        std::cerr << "Error: Computed index out of bounds!" << std::endl;
        return nullptr;
    }

    while (table[index].pointerKey_) {
        if (table[index].pointerKey_ == pointer) {
            return table[index].pointerKey_;
        }
        index = (index + 1) % capacity;
    }

    // If not found, return nullptr
    return nullptr;
}


// Insert a key-sized pair into the hash table
void HashTable::insertKey(void *key, size_t size) {
    if (entriesSize * 2 >= capacity) {
        growHashTable();
    }

    size_t index = hash(key) % capacity;

    while (table[index].pointerKey_ && table[index].pointerKey_ != reinterpret_cast<void *>(tombstone)) {
        index = (index + 1) % capacity;
    }
    table[index] = {key, size};
    entriesSize++;
}


// Remove a key from the hash table and return the associated size
size_t HashTable::removeKey(void *key) {
    size_t index = hash(key) % capacity;

    while (table[index].pointerKey_) {
        if (table[index].pointerKey_ == key) {
            size_t size = table[index].size_;
            table[index].pointerKey_ = reinterpret_cast<void *>(tombstone); // mark entry as deleted with a tombstone
            table[index].size_ = 0;
            entriesSize--;
            return size;
        }
        index = (index + 1) % capacity;
    }

    return 0; // If key was not found
}