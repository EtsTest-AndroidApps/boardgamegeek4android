package com.boardgamegeek.auth;

import com.boardgamegeek.util.HttpUtils;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class NetworkAuthenticator {
	@SuppressWarnings("FieldCanBeLocal") private static final boolean MOCK_LOGIN = false;

	private NetworkAuthenticator() {
	}

	/**
	 * Authenticates to BGG with the specified username and password, returning the cookie store to use on subsequent
	 * requests, or null if authentication fails.
	 */
	@Nullable
	public static BggCookieJar authenticate(@NonNull String username, @NonNull String password, @NonNull String method) {
		if (MOCK_LOGIN) {
			return BggCookieJar.getMock();
		} else {
			return tryAuthenticate(username, password, method);
		}
	}

	@Nullable
	private static BggCookieJar tryAuthenticate(@NonNull String username, @NonNull String password, @NonNull String method) {
		try {
			return performAuthenticate(username, password, method);
		} catch (@NonNull final IOException e) {
			logAuthFailure(method, "IOException");
		} finally {
			Timber.w("Authentication complete");
		}
		return null;
	}

	@Nullable
	private static BggCookieJar performAuthenticate(@NonNull String username, @NonNull String password, @NonNull String method) throws IOException {
		final BggCookieJar cookieJar = new BggCookieJar();
		final OkHttpClient client = HttpUtils.getHttpClient().newBuilder()
			.cookieJar(cookieJar)
			.build();
		Request post = buildRequest(username, password);
		final Response response = client.newCall(post).execute();
		if (response.isSuccessful()) {
			if (cookieJar.isValid()) {
				return cookieJar;
			} else {
				logAuthFailure(method, "Invalid cookie jar");
			}
		} else {
			logAuthFailure(method, "Response: " + response.toString());
		}
		return null;
	}

	private static void logAuthFailure(String method, String reason) {
		Timber.w("Failed %1$s login: %2$s", method, reason);
	}

	@NonNull
	private static Request buildRequest(@NonNull String username, @NonNull String password) {
		FormBody formBody = new FormBody.Builder()
			.add("username", username)
			.add("password", password)
			.build();
		return new Request.Builder()
			.url("https://www.boardgamegeek.com/login")
			.post(formBody)
			.build();
	}
}
