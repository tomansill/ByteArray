package com.ansill.arrays;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.ansill.arrays.IndexingUtility.combineVariadic;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * ByteArrays utility class used to wrap and combine primitive arrays or ByteBuffers and turn them into ByteArrays
 */
public final class ByteArrays {

	private ByteArrays() {
		throw new AssertionError(format("Instantiation of %s is not permitted", this.getClass().getSimpleName()));
	}

	/**
	 * Combines multiple {@link ReadOnlyByteArray}s into a single {@link ReadOnlyByteArray}
	 * <p>
	 * If a list containing a single {@link ReadOnlyByteArray} is passed in, then that same {@link ReadOnlyByteArray} will be returned.
	 * </p>
	 * <p>
	 * If there's any {@link ReadableWritableByteArray}s in the input list, it will be converted to {@link ReadOnlyByteArray} using {@link ReadableWritableByteArray}::toReadOnly method.
	 * </p>
	 *
	 * @param byteArrays a list of {@link ReadOnlyByteArray} to be combined
	 * @return a single {@link ReadOnlyByteArray}
	 */
	@Nonnull
	public static ReadOnlyByteArray combineReadOnly(@Nonnull List<? extends ReadOnlyByteArray> byteArrays) {

		// Assert non-empty
		requireNonNull(byteArrays, "ByteArrays list is null");
		if (byteArrays.isEmpty()) throw new IllegalArgumentException("ByteArrays list is empty");

		// Fast path: single element (also validates non-null)
		if (byteArrays.size() == 1) {
			ReadOnlyByteArray single = byteArrays.get(0);
			requireNonNull(single, "There is a null element in the ByteArray list");
			return (single instanceof ReadableWritableByteArray) ? ((ReadableWritableByteArray) single).toReadOnly() : single;
		}

		// Normalize + validate in one pass (no streams, no extra list unless needed)
		List<ReadOnlyByteArray> normalized = new ArrayList<>(byteArrays.size());
		for (ReadOnlyByteArray byteArray : byteArrays) {
			requireNonNull(byteArray, "There is a null element in the ByteArray list");
			if (byteArray instanceof ReadableWritableByteArray) {
				ReadableWritableByteArray rw = (ReadableWritableByteArray) byteArray;
				normalized.add(rw.toReadOnly());
			} else {
				normalized.add(byteArray);
			}
		}
		return new ReadOnlyMultipleByteArray(normalized);
	}

	/**
	 * Combines multiple {@link ReadOnlyByteArray}s into a single {@link ReadOnlyByteArray}
	 * <p>
	 * If there's any {@link ReadableWritableByteArray}s in the input arguments, it will be converted to {@link ReadOnlyByteArray} using {@link ReadableWritableByteArray}::toReadOnly method.
	 * <p>
	 * Example:
	 * <pre>{@code
	 * var twoCombo = ByteArray.combine(ByteArray.wrap(new byte[25]).toReadOnly(), ByteArray.wrap(ByteBuffer.allocate(25)).toReadOnly());
	 * var moreCombo = ByteArray.combine(ByteArray.wrap(new byte[25]).toReadOnly(), ByteArray.wrap(ByteBuffer.allocate(25)).toReadOnly(), twoCombo);
	 * }</pre>
	 *
	 * @param firstByteArray  first byte array to be combined
	 * @param secondByteArray second byte array to be combined
	 * @param restByteArrays  rest of byte arrays to be combined. <i>(Optional. Can be blank.)</i>
	 * @return a single {@link ReadOnlyByteArray}
	 */
	@Nonnull
	public static ReadOnlyByteArray combine(
					@Nonnull ReadOnlyByteArray firstByteArray,
					@Nonnull ReadOnlyByteArray secondByteArray,
					@Nonnull ReadOnlyByteArray... restByteArrays
	) {

		// Combine parameters
		var byteArrays = combineVariadic(firstByteArray, secondByteArray, restByteArrays);

		// Make sure all are readonly. Convert if any of them is not
		for (int index = 0; index < byteArrays.size(); index++) {
			var byteArray = byteArrays.get(index);
			if (byteArray instanceof ReadableWritableByteArray) {
				byteArray = ((ReadableWritableByteArray) byteArray).toReadOnly();
				byteArrays.set(index, byteArray);
			}
		}

		// Return
		return new ReadOnlyMultipleByteArray(byteArrays);
	}

	/**
	 * Combines multiple {@link ReadableWritableByteArray}s into a single {@link ReadableWritableByteArray}
	 * <p>
	 * If a list containing a single {@link ReadableWritableByteArray} is passed in, then that same {@link ReadableWritableByteArray} will be returned.
	 * </p>
	 *
	 * @param byteArrays a list of {@link ReadableWritableByteArray}s to be combined
	 * @return a single {@link ReadableWritableByteArray}
	 */
	@Nonnull
	public static ReadableWritableByteArray combineReadableWritable(@Nonnull List<? extends ReadableWritableByteArray> byteArrays) {

		// Assert non-empty
		requireNonNull(byteArrays, "ByteArrays list is null");
		if (byteArrays.isEmpty()) throw new IllegalArgumentException("ByteArrays list is empty");
		for (var byteArray : byteArrays) requireNonNull(byteArray, "There is a null element in the ByteArray list");

		// If only one, return that one
		if (byteArrays.size() == 1) return byteArrays.get(0);

		// Return
		return new ReadableWritableMultipleByteArray(byteArrays);
	}

	/**
	 * Combines multiple {@link ReadableWritableByteArray}s into a single {@link ReadableWritableByteArray}
	 * <p>
	 * Example:
	 * <pre>{@code
	 * var twoCombo = ByteArray.combine(ByteArray.wrap(new byte[25]), ByteArray.wrap(ByteBuffer.allocate(25)));
	 * var moreCombo = ByteArray.combine(ByteArray.wrap(new byte[25]), ByteArray.wrap(ByteBuffer.allocate(25)), twoCombo);
	 * }</pre>
	 *
	 * @param firstByteArray  first byte array to be combined
	 * @param secondByteArray second byte array to be combined
	 * @param restByteArrays  rest of byte arrays to be combined. <i>(Optional. Can be blank.)</i>
	 * @return a single {@link ReadableWritableByteArray}
	 */
	@Nonnull
	public static ReadableWritableByteArray combine(
					@Nonnull ReadableWritableByteArray firstByteArray,
					@Nonnull ReadableWritableByteArray secondByteArray,
					@Nonnull ReadableWritableByteArray... restByteArrays
	) {

		// Combine parameters
		var byteArrays = combineVariadic(firstByteArray, secondByteArray, restByteArrays);

		// Return
		return new ReadableWritableMultipleByteArray(byteArrays);
	}

	/**
	 * Wraps one or more primitive byte arrays into {@link ReadableWritableByteArray}
	 * <p>
	 * Example:
	 * <pre>{@code
	 * var byteArrayOfOneByteArray = ByteArray.wrap(new byte[100]);
	 * var byteArrayOfTwoByteArrays = ByteArray.wrap(new byte[25], new byte[75]);
	 * var byteArrayOfMoreByteArrays = ByteArray.wrap(new byte[25], new byte[23], new byte[33], new byte[10]);
	 * }</pre>
	 *
	 * @param firstByteArray first primitive byte array to be wrapped
	 * @param restByteArrays more primitive byte arrays to be wrapped. <i>(Optional. Can be blank.)</i>
	 * @return a single {@link ReadableWritableByteArray} containing all byte arrays that has been passed in.
	 */
	@Nonnull
	public static ReadableWritableByteArray wrap(@Nonnull byte[] firstByteArray, @Nonnull byte[]... restByteArrays) {

		// Check first
		requireNonNull(firstByteArray, "first element is null");

		// Check rest
		requireNonNull(restByteArrays, "rest array is null");
		for(byte[] byteArray : restByteArrays) requireNonNull(byteArray, "null elements in rest array");

		// Short circuit
		if(restByteArrays.length == 0) return new PrimitiveByteArray(firstByteArray);

		// Create list
		var list = new ArrayList<PrimitiveByteArray>(1 + restByteArrays.length);
		list.add(new PrimitiveByteArray(firstByteArray));
		for (byte[] byteArray : restByteArrays) {
			list.add(new PrimitiveByteArray(byteArray));
		}

		// Return
		return new ReadableWritableMultipleByteArray(list);
	}

	/**
	 * Wraps one or more {@link ByteBuffer}s into {@link ReadableWritableByteArray}
	 * <p>
	 * Note that {@link ByteBuffer}'s position() and limit() markers will be respected. The {@link ByteBuffer} will
	 * be duplicated using duplicate() method so any changes to position() and limit() markers on the original
	 * {@link ByteBuffer} will not affect the wrapped {@link ByteBuffer}.
	 * <p>
	 * Example:
	 * <pre>{@code
	 * var byteArrayOfOneByteArray = ByteArray.wrap(ByteBuffer.allocate(100));
	 * var byteArrayOfTwoByteArrays = ByteArray.wrap(ByteBuffer.allocate(25), ByteBuffer.allocate(75));
	 * var byteArrayOfMoreByteArrays = ByteArray.wrap(ByteBuffer.allocate(25), ByteBuffer.allocate(23), ByteBuffer.allocate(33), ByteBuffer.allocate(10));
	 * }</pre>
	 *
	 * @param firstByteBuffer first {@link ByteBuffer} to be wrapped
	 * @param restByteBuffers more {@link ByteBuffer}s to be wrapped. <i>(Optional. Can be blank.)</i>
	 * @return a single {@link ReadableWritableByteArray} containing all {@link ByteBuffer}s that has been passed in.
	 */
	@Nonnull
	public static ReadableWritableByteArray wrap(@Nonnull ByteBuffer firstByteBuffer, @Nonnull ByteBuffer... restByteBuffers) {

		// Check first
		requireNonNull(firstByteBuffer, "first element is null");

		// Check rest
		requireNonNull(restByteBuffers, "rest array is null");
		for(ByteBuffer byteBuffer : restByteBuffers) requireNonNull(byteBuffer, "null elements in rest array");

		// Check first
		if(firstByteBuffer.isReadOnly()) throw new IllegalArgumentException("ReadOnly ByteBuffer was passed in");

		// Short circuit
		if(restByteBuffers.length == 0) return new ByteBufferByteArray(firstByteBuffer);

		// Create list
		var list = new ArrayList<ByteBufferByteArray>(1 + restByteBuffers.length);
		list.add(new ByteBufferByteArray(firstByteBuffer));
		for (ByteBuffer restByteBuffer : restByteBuffers) {
			if (restByteBuffer.isReadOnly()) throw new IllegalArgumentException("ReadOnly ByteBuffer was passed in");
			list.add(new ByteBufferByteArray(restByteBuffer));
		}

		// Return
		return new ReadableWritableMultipleByteArray(list);
	}
}
