# ByteArray

## Description

A robust Java library that provides convenient and secure classes to handle `byte[]` arrays and `ByteBuffer`s with
fine-grained read-only, write-only, and readable-writable accesses and ability to easily create subsets of the byte
array data.

## Motivation

The motivation was that I had a large binary data file which contained rows of records and the structure of those
records
is not known ahead of the time. And due to performance reasons, I had to rely on reading the data file with large
`ByteBuffer`s. I often ran into issues of having `ByteBuffer` that reads and stops at the middle of a record, and I
have to use two `ByteBuffer`s to join and link the split row data which is not easy to do and may require another
copying of bytes. `ByteBuffer` is an abstract class, so I can't easily extend it to support the transparent joining of
two `ByteBuffer`s. When I do try to extend it, `ByteBuffer` contains a method that is `package-private` which prevents
me from properly extending it in my own package. Moving my extended `ByteBuffer` subclass into `java.nio` package is
not a good option. Another issue is that `ByteBuffer` is readable-writable by default and while you can make it
read-only by calling `ByteBuffer::asReadOnlyBuffer`, but you can still make a mistake of calling any `put` commands and
have it throw `ReadOnlyBufferException`. There ought to be some way to enforce the read-only property at compile time.

I knew that I needed a new class that starts out as `interface`, so it can be overloaded by anyone else, has read-only,
write-only, and readable-writable versions of classes so those read-only/write-only properties can be enforced at
compile time, ability to easily sub-set the data to indicate start/end of the record, and ability to join multiple data
together and represent it as a single array of data.

## Features

- `interface`-first classes
    - Easily extendable by anyone to fit their needs
    - Initial support of `byte[]` arrays and `ByteBuffer`s
- Compile-time access controls
    - Read-only class
    - Write-only class
    - Readable-Writable class
- Ability to subset data
- Ability to join multiple arrays of data
- 64-bit addressing space support

## Available classes

- `ByteArray`
  - Provides the base interface of a byte array
  - Provides `subsetOf(long,long)` and `size()` methods
  - `static` methods to create `ByteArray`s using `byte[]` arrays or `ByteBuffer`s and joining of multiple `ByteArray`
    s
- `ReadOnlyByteArray`
  - Read-only version of `ByteArray`
  - Provides `readByte(long)` and `read(long, WriteOnlyByteArray)` methods to read the bytes from
    the `ReadOnlyByteArray`
- `WriteOnlyByteArray`
    - Write-only version of `ByteArray`
    - Provides `writeByte(long, byte)` and `write(long, ReadOnlyByteArray)` methods to write the bytes to
      the `WriteOnlyByteArray`
- `ReadableWritableByteArray`
    - Readable-writable version of `ByteArray`
    - Combination of `ReadOnlyByteArray` and `WriteOnlyByteArray` interfaces
    - Provides `readByte(long)`, `read(long, WriteOnlyByteArray)`, `writeByte(long, byte)`,
      and `write(long, ReadOnlyByteArray)` methods to fully read or write the bytes to `ReadableWritableByteArray`
    - Provides `toReadOnly()` and `toWriteOnly()` methods to convert `ReadableWritableByteArray` to
      either `ReadOnlyByteArray` or `WriteOnlyByteArray` to lock out accesses

## Examples

### Wrapping `byte[]` array and manipulating it

```
// Create the original byte array
var bytes = new byte[]{22, 9, 20};

// Wrap it and create ReadableWritableByteArray
var byteArray = ByteArray.wrap(bytes);

// Update 2nd element on bytes array
byteArray.writeByte(1, 5); // This will also update 'bytes' array because it's the 'backing' data

// Assert
assert byteArray.readByte(1) == 5;
assert bytes[1] == 5;
```

### Wrapping and join multiple `ByteBuffer`s and using `subsetOf(long,long)` to get a view of desired data portion in the data

```
// Create large bytebuffers
var byteBuffer1 = ByteBuffer.allocate(1_000_000);
var byteBuffer2 = ByteBuffer.allocate(1_000_000);

// Read 2MB of data data into bytebuffers
readData(byteBuffer1);
readData(byteBuffer2);

// Example: Our desired row is between the buffers. 
// (First some bytes on the end of first ByteBuffer and the rest of bytes on the beginning of second ByteBuffer.)

// Wrap both buffers
var byteArray1 = ByteArray.wrap(byteBuffer1);
var byteArray2 = ByteArray.wrap(byteBuffer2);

// Combine the data by joining byte arrays
var largeByteArray = ByteArray.combine(byteArray1, byteArray2);

// Subset the large byte array to get data of our desired row
var rowData1 = largeByteArray.subsetOf(999_000, 2_000); // 2,000 bytes long ByteArray where 1,000 bytes each from both buffers
assert rowData1.size() == 2_000;

// Another quicker way of same example:
var rowData2 = ByteArray.wrap(byteBuffer1, byteBuffer2).subset(999_000, 2_000); // wrap method is a variadic method, can accept as many `ByteBuffer`s as you can fit.
assert rowData1.size() == 2_000;
```

### Access Control Demonstration

```
// Create example byte arrays
var bytes1 = new byte[]{1, 2, 3, 4};
var bytes2 = new byte[]{5, 6, 7, 8};

// Create ReadableWritableByteArray
var rwByteArray = ByteArray.wrap(bytes1, bytes2); // wrap method is a variadic method, can accept as many `byte[]`s as you can fit.

// ReadableWritableByteArray is fully readable-writable so you can read and write whatever you want
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

### `Copying ByteArray`s

```
// Create example byte arrays
var bytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};

// Create ByteArray
var largeByteArray = ByteArray.wrap(bytes);

// Create destination ByteBuffer to fill data while reading largeByteArray
var smallerBB = ByteBuffer.allocate(3);
var destination = ByteArray.wrap(smallerBB);

// Read bytes from largeByteArray into destination byte array
largeByteArray.read(2, destination);

// Both smallerBB and destination should have 3, 4, 5 in them after that read call

// Create source ByteBuffer to write data to largeByteArray
var smallerBytes = new byte[]{4, 3, 2, 1};
var source = ByteArray.wrap(smallerBytes);

// Write bytes from source byte array into largeByteArray
largeByteArray.write(4, source);

// bytes and largeByteArray should have 1, 2, 3, 4, 4, 3, 2, 1 after that write call

// Have largeByteArray to subset and read its contents to source bytearray
largeByteArray.subsetOf(2, 4).read(0, source);

// source and smallerBytes should now have 3, 4, 4, 3 after that read call
```

## TO-DOs

- Implement the remaining read/write calls
  - `readShort(long)`
  - `readInt(long)`
  - `readLong(long)`
  - `readFloat(long)`
  - `readDouble(long)`
  - `writeShort(long,short)`
  - `writeInt(long,int)`
  - `writeLong(long,long)`
  - `writeFloat(long,float)`
  - `writeDouble(long,double)`
- Endian-ness support?
  - Use `java.nio.ByteOrder`?
  - `reverse()` method or `to(java.nio.ByteOrder)` method? *(less work as all read/writes shall obey one endianness
    setting)*
  - Overloaded read/write methods to include `java.nio.ByteOrder` to enable individual read/writes with endianness
    control? *(more work)*
  - `getByteOrder()` method?
- Support more backing data types?