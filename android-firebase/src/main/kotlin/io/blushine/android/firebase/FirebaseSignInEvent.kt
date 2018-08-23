package io.blushine.android.firebase

import com.firebase.ui.auth.ErrorCodes
import com.google.firebase.auth.FirebaseUser

/**
 * Event for when the user tried to sign in through Firebase
 */
data class FirebaseSignInEvent(val status: Statuses, @ErrorCodes.Code val errorCode: Int = -1, val firebaseUser: FirebaseUser? = null) {
	/**
	 * The various responses from the statuses
	 */
	enum class Statuses {
		SIGNED_IN,
		CANCELED_BY_USER,
		ERROR
	}
}
