package io.blushine.android.firebase

import android.app.Activity.RESULT_OK
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseUser
import com.squareup.otto.Subscribe
import io.blushine.android.ActivityResultEvent
import io.blushine.android.AppActivity
import io.blushine.utils.EventBus
import java.util.*

/**
 * Wrapper class for authenticating users in Firebase
 */
object FirebaseAuth {
	private val RC_SIGN_IN = 5627

	init {
		EventBus.getInstance().register(this)
	}

	/**
	 * Shows the sign in screen for the user.
	 * A [FirebaseSignInEvent] is posted to the [EventBus] when the user has successfully
	 * signed in, pressed back, or an error occurred.
	 */
	fun signIn() {
		// Authenticators
		val providers = Arrays.asList(
				AuthUI.IdpConfig.GoogleBuilder().build(),
				AuthUI.IdpConfig.EmailBuilder().build(),
				AuthUI.IdpConfig.PhoneBuilder().build()
		)

		val signInIntent = AuthUI.getInstance().createSignInIntentBuilder()
				.setAvailableProviders(providers)
				.build()

		AppActivity.getActivity()?.startActivityForResult(signInIntent, RC_SIGN_IN)
	}

	/**
	 * Sign out the user
	 */
	fun signOut() {
		AuthUI.getInstance().signOut(AppActivity.getActivity());
	}


	/**
	 * Delete the user. Be sure to delete the user's data in databases or cloud storage before deleting the
	 * user account.
	 */
	fun deleteUser() {
		// TODO
	}

	/**
	 * Get the current user
	 * @return current logged in user, null if none is logged in
	 */
	fun getCurrentUser(): FirebaseUser? {
		return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser;
	}

	@Subscribe
	fun onActivityResultEvent(event: ActivityResultEvent) {
		if (event.requestCode == RC_SIGN_IN) {
			val response = IdpResponse.fromResultIntent(event.data)

			val firebaseSignInEvent: FirebaseSignInEvent

			// Successfully signed in
			if (event.resultCode == RESULT_OK) {
				firebaseSignInEvent = FirebaseSignInEvent(FirebaseSignInEvent.Statuses.SIGNED_IN, firebaseUser = getCurrentUser())
			}
			// Pressed Back
			else if (response == null) {
				firebaseSignInEvent = FirebaseSignInEvent(FirebaseSignInEvent.Statuses.CANCELED_BY_USER)
			}
			// Error
			else {
				firebaseSignInEvent = FirebaseSignInEvent(FirebaseSignInEvent.Statuses.ERROR,
														  response.error?.errorCode ?: -1)
			}

			EventBus.getInstance().post(firebaseSignInEvent)
		}
	}
}