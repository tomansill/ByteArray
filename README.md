# ByteArray

## Description

A Java library that provides convenient and safe classes for working with `byte[]` arrays and `ByteBuffer`s, offering
fine-grained read-only, write-only, and readable-writable access modes, along with support for creating lightweight 
views (subsets) of the byte array data.

## Install

Just install using Maven 3:

`mvn clean install -DskipTests`

Omit `-DskipTests` to include tests in the build. Note that tests are long-running and may take significant time to 
complete.

## Motivation

The motivation came from working with large binary data file containing rows of records whose structure is not known 
ahead of the time. For performance reasons, I relied on reading these files using large `ByteBuffer`s. 
I frequently ran into issues where a `ByteBuffer` would stop in the middle of a record, requiring me to manually stitch 
together data from two buffers. This made handling records awkward and often introduced extra byte copying or additional
bookkeeping.
`ByteBuffer` is an abstract class, which makes it difficult to extend in a meaningful way. In particular, parts of its 
internal implementation rely on package-private methods, preventing clean extension outside of `java.nio` package. 
Moving custom implementation into `java.nio` is not a practical option.

Another issue is that `ByteBuffer` is mutable by default. While it can be made read-only using `asReadOnlyBuffer()`,
this is only enforced at runtime and can easily lead to mistakes where `put` operations result in 
`ReadOnlyBufferException`. There is no compile-time guarantee for access restriction.

This led to the need for a new design based on an interface-first approach. The goal was to support read-only, 
write-only, and read-write buffer types with compile-time enforcement, along with the ability to define sub-ranges of 
data to represent records and to compose multiple buffers into a single logical view of contiguous data.

## Features

- `interface`-first design
    - Easily extendable to fit custom use cases
    - Initial support of `byte[]` arrays and `ByteBuffer`s
- Compile-time access controls
    - Read-only class
    - Write-only class
    - Readable-Writable class
- Support for subsetting data
- Support for joining multiple arrays of data
- 64-bit addressing space support

## Available classes

- `ByteArray`
    - Base interface for byte array access
    - Supports `subsetOf(long,long)` and `size()` methods
- `ByteArrays`
  - Static factory methods for creating instances from `byte[]`, `ByteBuffer`s, and for joining multiple `ByteArray`s
    - `wrap(byte[])` -> `ReadableWritableByteArray`
    - `wrap(ByteBuffer)` -> `ReadableWritableByteArray`
    - `combine(ReadableWritableByteArray,ReadableWritableByteArray,ReadableWritableByteArray...)` -> `ReadableWritableByteArray`
    - `combine(ReadOnlyByteArray,ReadOnlyByteArray,ReadOnlyByteArray...)` -> `ReadOnlyByteArray`
    - `combineReadableWritable(List<? extends ReadableWritableByteArray>)` -> `ReadableWritableByteArray`
    - `combineReadOnly(List<? extends ReadOnlyByteArray>)` -> `ReadOnlyByteArray`
- `ReadOnlyByteArray`
    - Read-only view of `ByteArray`
    - Provides methods for reading `ByteArray`s and primitive values in big-endian (BE) and little-endian (LE) formats:
      - `copyTo(long, WriteOnlyByteArray)`
      - `readByte(long)`
      - `readShortBE(long)`
      - `readShortLE(long)`
      - `readIntBE(long)`
      - `readIntLE(long)`
      - `readLongBE(long)`
      - `readLongLE(long)`
      - `readFloatBE(long)`
      - `readFloatLE(long)`
      - `readDoubleBE(long)`
      - `readDoubleLE(long)`
    - **IMPORTANT:** Not strictly immutable version. If the underlying data or any associated 
      `ReadableWritableByteArray` is modified, changes are reflected in this view. It can only be made 
      *practically immutable* if the backing data is protected from mutation.
- `WriteOnlyByteArray`
    - Write-only version of `ByteArray`
    - Provides methods for writing `ByteArray`s and primitive values in big-endian (BE) and little-endian (LE) formats:
      - `copyFrom(long, ReadOnlyByteArray)`
      - `writeByte(long, byte)`
      - `writeShortBE(long, short)`
      - `writeShortLE(long, short)`
      - `writeIntBE(long, int)`
      - `writeIntLE(long, int)`
      - `writeLongBE(long, long)`
      - `writeLongLE(long, long)`
      - `writeFloatBE(long, float)`
      - `writeFloatLE(long, float)`
      - `writeDoubleBE(long, double)`
      - `writeDoubleLE(long, double)`
- `ReadableWritableByteArray`
    - Read-write version of `ByteArray`
    - Combination of `ReadOnlyByteArray` and `WriteOnlyByteArray` interfaces which includes all read/copyTo and write/copyFrom methods
    - Provides `toReadOnly()` and `toWriteOnly()` methods to convert `ReadableWritableByteArray` to
      either `ReadOnlyByteArray` or `WriteOnlyByteArray` to restrict access at compile-time.

## Examples

### Wrapping `byte[]` array and manipulating it

```java
// Create the original byte array
var bytes = new byte[]{22, 9, 20};

// Wrap it and create ReadableWritableByteArray
var byteArray = ByteArrays.wrap(bytes);

// Update 2nd element on bytes array
byteArray.writeByte(1, 5); // This will also update 'bytes' array because it's the 'backing' data

// Assert
assert byteArray.readByte(1) == 5;
assert bytes[1] == 5;
```

### Wrapping and join multiple `ByteBuffer`s and using `subsetOf(long,long)` to get a view of desired data portion in the data

```java
// Create large bytebuffers
var byteBuffer1 = ByteBuffer.allocate(1_000_000);
var byteBuffer2 = ByteBuffer.allocate(1_000_000);

// Example read 2MB of data into bytebuffers
readData(byteBuffer1);
readData(byteBuffer2);

// Example: Our desired row is between the buffers. 
// (First some bytes on the end of first ByteBuffer and the rest of bytes on the beginning of second ByteBuffer.)

// Wrap both buffers
var byteArray1 = ByteArrays.wrap(byteBuffer1);
var byteArray2 = ByteArrays.wrap(byteBuffer2);

// Combine the data by joining byte arrays
var largeByteArray = ByteArrays.combine(byteArray1, byteArray2);

// Subset the large byte array to get data of our desired row
var rowData1 = largeByteArray.subsetOf(999_000, 2_000); // 2,000 bytes long ByteArray where 1,000 bytes each from both buffers
assert rowData1.size() == 2_000;

// Another quicker way of same example:
var rowData2 = ByteArrays.wrap(byteBuffer1, byteBuffer2).subsetOf(999_000, 2_000); // wrap method is a variadic method, can accept as many `ByteBuffer`s as you can fit.
assert rowData1.size() == 2_000;
```

### Compile-time Access Control Demonstration

```java
// Create example byte arrays
var bytes1 = new byte[]{1, 2, 3, 4};
var bytes2 = new byte[]{5, 6, 7, 8};

// Create ReadableWritableByteArray
var rwByteArray = ByteArrays.wrap(bytes1, bytes2); // wrap method is a variadic method, can accept as many `byte[]`s as you can fit.

// ReadableWritableByteArray is fully read-write so you can read and write whatever you want
assert rwByteArray.readByte(5) == 6;
rwByteArray.writeByte(5, -6); // This updates 2nd element in bytes2 because bytes2 is the backing data of this ReadableWritableByteArray
assert rwByteArray.readByte(5) == -6;
assert bytes2[1] == -6;

// Create read-only view
var readonly = rwByteArray.toReadOnly();
assert readonly.readByte(5) == -6;
// readonly.writeByte(5, 12); // Compile error because writeByte(long,byte) does not exist in ReadOnlyByteArray

// Create write-only view
var writeonly = rwByteArray.toWriteOnly();
writeonly.writeByte(2, 30); // This updates to rwByteArray's 3rd element and also updates to bytes1 arrays' 3rd element.
// writeonly.readByte(2); // Compile error because readByte(long) does not exist in WriteOnlyByteArray

// Assertions to prove that writes to write-only ByteArray will update to all related data
assert readonly.readByte(2) == 30;
assert rwByteArray.readByte(2) == 30;
assert bytes1[2] == 30;
```

### Copying `ByteArray`s

```java
// Create example byte arrays
var bytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};

// Create ByteArray
var largeByteArray = ByteArrays.wrap(bytes);

// Create destination ByteBuffer to fill data while reading largeByteArray
var smallerBB = ByteBuffer.allocate(3);
var destination = ByteArrays.wrap(smallerBB);

// Read bytes from largeByteArray into destination byte array
largeByteArray.read(2, destination);

// Both smallerBB and destination should have 3, 4, 5 in them after that read call

// Create source ByteBuffer to write data to largeByteArray
var smallerBytes = new byte[]{4, 3, 2, 1};
var source = ByteArrays.wrap(smallerBytes);

// Write bytes from source byte array into largeByteArray
largeByteArray.write(4, source);

// bytes and largeByteArray should have 1, 2, 3, 4, 4, 3, 2, 1 after that write call

// Have largeByteArray to subset and read its contents to source bytearray
largeByteArray.subsetOf(2, 4).read(0, source);

// source and smallerBytes should now have 3, 4, 4, 3 after that read call
```