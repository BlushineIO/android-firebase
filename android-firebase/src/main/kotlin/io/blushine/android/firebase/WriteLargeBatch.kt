package io.blushine.android.firebase

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import io.blushine.utils.MutablePair

private val MAX_BATCH_WRITES = 500

/**
 * Wraps multiple [WriteBatch] into one to allow for batches
 * larger containing more than 500 documents.
 */
class WriteLargeBatch(private val firestore: FirebaseFirestore) {
	private val batches = ArrayList<MutablePair<WriteBatch, Int>>(1)
	private var commited = false
	private val internalTasks = ArrayList<Task<Void>>()

	init {
		batches.add(MutablePair(firestore.batch(), 0))
	}

	/**
	 * Get an available [WriteBatch] for writing one document. This method also increments
	 * the number of documents in the [batches] Pair
	 */
	private fun getAvailableBatch(): WriteBatch {
		if (batches.last().second == MAX_BATCH_WRITES) {
			batches.add(MutablePair(firestore.batch(), 0))
		}

		batches.last().second += 1
		return batches.last().first
	}

	/**
	 * Check if the [commit] has been called. If it has, throw an exception since we can't use
	 * a batch that has been commit
	 */
	private fun checkForCommit() {
		if (commited) {
			throw IllegalStateException("A write batch can no longer be used after commit() has been called.")
		}
	}

	fun set(documentRef: DocumentReference, data: Map<String, Any>): WriteLargeBatch {
		checkForCommit()
		getAvailableBatch().set(documentRef, data)
		return this
	}

	fun set(documentRef: DocumentReference, data: Map<String, Any>, options: SetOptions): WriteLargeBatch {
		checkForCommit()
		getAvailableBatch().set(documentRef, data, options)
		return this
	}

	fun set(documentRef: DocumentReference, pojo: Any): WriteLargeBatch {
		checkForCommit()
		getAvailableBatch().set(documentRef, pojo)
		return this
	}

	fun set(documentRef: DocumentReference, pojo: Any, options: SetOptions): WriteLargeBatch {
		checkForCommit()
		getAvailableBatch().set(documentRef, pojo, options)
		return this
	}

	fun update(documentRef: DocumentReference, data: Map<String, Any>): WriteLargeBatch {
		checkForCommit()
		getAvailableBatch().update(documentRef, data)
		return this
	}

	fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any): WriteLargeBatch {
		checkForCommit()
		getAvailableBatch().update(documentRef, field, value, moreFieldsAndValues)
		return this
	}

	fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any): WriteLargeBatch {
		checkForCommit()
		getAvailableBatch().update(documentRef, fieldPath, value, moreFieldsAndValues)
		return this
	}

	fun delete(documentRef: DocumentReference): WriteLargeBatch {
		checkForCommit()
		getAvailableBatch().delete(documentRef)
		return this
	}

	fun commit(): Task<Void> {
		for (batchPair in batches) {
			val batch = batchPair.first
			val batchTask = batch.commit()
			internalTasks.add(batchTask)
		}

		return Tasks.whenAll(internalTasks)
	}
}

fun FirebaseFirestore.largeBatch(): WriteLargeBatch {
	return WriteLargeBatch(this)
}
