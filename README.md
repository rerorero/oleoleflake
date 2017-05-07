# oleoleflake

[![](https://jitpack.io/v/rerorero/oleoleflake.svg)](https://jitpack.io/#rerorero/oleoleflake)
[![Build Status](https://travis-ci.org/rerorero/oleoleflake.svg?branch=master)](https://travis-ci.org/rerorero/oleoleflake)

A customizable distributed unique ID generator inspired by [Twitter's Snowflake](https://github.com/twitter/snowflake).

More functions are as follows.

- Bit field length and layout can be customized.
- Field values (timestamp, machine id, etc) can be easily parsed from ID.
- ID generation with the specified value (snapshot function). This is useful for creating SQL queries that you want to filter with a range of the timestamp field of ID.
- Transparently handle inverted bits.

## Examples
```java
// Get Long(64bit) ID generator. That is thread-safe.
Id64Gen snowflake = Id64Gen.builder()
  .nextBit(1).unusedField() // Sign Bit
  .nextBit(41).timestampField()
    .tickPerMillisec()
    .startAt(Instant.parse("2017-01-01T00:00:00.00Z"))
  .nextBit(5).constantField()
    .name("data-center-id").value(1)
  .nextBit(5).constantField()
    .name("worker-id").value(2)
  .nextBit(12).sequenceField()
  .build();

// Generate unique ID.
Long id = snowflake.next().id();

// Parse timestamp.
Instant parsedTime = snowflake.parseTimestamp(id);

// Get ID with specified values.
Long id2 = snowflake.snapshot()
        .putTimestamp(Instant.parse("2017-01-01T00:00:00.00Z"))
        .putSequence(Long.valueOf(100))
        .id();
```

## Installation
This library is release in JitPack. Latest version is [![](https://jitpack.io/v/rerorero/oleoleflake.svg)](https://jitpack.io/#rerorero/oleoleflake)

You can see how to add this library to your project on the [JitPack page](https://jitpack.io/#rerorero/oleoleflake).

## Field Types
You can combine the following field types. Following a specifiying the bit length with `nextBit()`, call the function that generates the field builder to select the field type.

All field builders transparently support bit flipping by the `flip()` function.

### Timestamp Field
- `timestampField()` creates a timestamp field builder.
- ID can contain at most one of this field.
- `startAt()` tells epoch to the generator. Required.
- `tickPerMilliSec()` or `tickPerSecond()` defines the tick. Also, if you want to customize the tick, use `withTimestampGenerator()`

### Sequence Field
- `sequenceField()` creates a sequence field builder.
- ID can contain at most one of this field.
- `startAt()` defines the origin of sequence. Default is 0.
- You can customize sequencer with `sequencer()`.

### Constant Field
- `constantField()` creates a constant value field builder.
- ID can contain any number of this field.
- This field has `name()` and `value()`. ID Generator always sets a fixed `value()` in the bit field.
- Name is used for parsing.

### Bindable Field
- `bindableField()` creates a bindable value field builder.
- ID can contain any number of this field.
- You need to specify the 'bindable' value each time an ID is generated for the ID containing bindable field.
- `name()` defines the field name.
```java
Id64Gen gen = Id64Gen.builder()
  .nextBit(1).unusedField()
  .nextBit(31).bindableField().name("worker")
  .nextBit(32).sequenceField()
  .build();

// Generates id with specified worker.
Long id = gen.next().bind("worker", 1).id();
```
