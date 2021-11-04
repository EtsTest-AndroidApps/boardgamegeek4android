package com.boardgamegeek.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.boardgamegeek.R;
import com.boardgamegeek.entities.ForumEntity.ForumType;
import com.boardgamegeek.provider.BggContract;
import com.boardgamegeek.util.ActivityUtils;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

public class ThreadActivity extends SimpleSinglePaneActivity {
	private static final String KEY_FORUM_ID = "FORUM_ID";
	private static final String KEY_FORUM_TITLE = "FORUM_TITLE";
	private static final String KEY_OBJECT_ID = "OBJECT_ID";
	private static final String KEY_OBJECT_NAME = "OBJECT_NAME";
	private static final String KEY_OBJECT_TYPE = "OBJECT_TYPE";
	private static final String KEY_THREAD_ID = "THREAD_ID";
	private static final String KEY_THREAD_SUBJECT = "THREAD_SUBJECT";

	private int threadId;
	private String threadSubject;
	private int forumId;
	private String forumTitle;
	private int objectId;
	private String objectName;
	private ForumType objectType;

	public static void start(Context context, int threadId, String threadSubject, int forumId, String forumTitle, int objectId, String objectName, ForumType objectType) {
		Intent starter = createIntent(context, threadId, threadSubject, forumId, forumTitle, objectId, objectName, objectType);
		context.startActivity(starter);
	}

	public static void startUp(Context context, int threadId, String threadSubject, int forumId, String forumTitle, int objectId, String objectName, ForumType objectType) {
		Intent starter = createIntent(context, threadId, threadSubject, forumId, forumTitle, objectId, objectName, objectType);
		starter.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(starter);
	}

	@NonNull
	private static Intent createIntent(Context context, int threadId, String threadSubject, int forumId, String forumTitle, int objectId, String objectName, ForumType objectType) {
		Intent starter = new Intent(context, ThreadActivity.class);
		starter.putExtra(KEY_THREAD_ID, threadId);
		starter.putExtra(KEY_THREAD_SUBJECT, threadSubject);
		starter.putExtra(KEY_FORUM_ID, forumId);
		starter.putExtra(KEY_FORUM_TITLE, forumTitle);
		starter.putExtra(KEY_OBJECT_ID, objectId);
		starter.putExtra(KEY_OBJECT_NAME, objectName);
		starter.putExtra(KEY_OBJECT_TYPE, objectType);
		return starter;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			if (TextUtils.isEmpty(objectName)) {
				actionBar.setTitle(forumTitle);
				actionBar.setSubtitle(threadSubject);
			} else {
				actionBar.setTitle(threadSubject + " - " + forumTitle);
				actionBar.setSubtitle(objectName);
			}
		}
	}

	@Override
	protected void readIntent(Intent intent) {
		threadId = intent.getIntExtra(KEY_THREAD_ID, BggContract.INVALID_ID);
		threadSubject = intent.getStringExtra(KEY_THREAD_SUBJECT);
		forumId = intent.getIntExtra(KEY_FORUM_ID, BggContract.INVALID_ID);
		forumTitle = intent.getStringExtra(KEY_FORUM_TITLE);
		objectId = intent.getIntExtra(KEY_OBJECT_ID, BggContract.INVALID_ID);
		objectName = intent.getStringExtra(KEY_OBJECT_NAME);
		objectType = (ForumType) intent.getSerializableExtra(KEY_OBJECT_TYPE);
	}

	@Override
	protected Fragment onCreatePane(Intent intent) {
		return ThreadFragment.newInstance(threadId, forumId, forumTitle, objectId, objectName, objectType);
	}

	@Override
	protected int getOptionsMenuId() {
		return R.menu.view_share;
	}

	@Override
	public boolean onOptionsItemSelected(@NotNull MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				ForumActivity.startUp(this, forumId, forumTitle, objectId, objectName, objectType);
				finish();
				return true;
			case R.id.menu_view:
				ActivityUtils.linkToBgg(this, "thread", threadId);
				return true;
			case R.id.menu_share:
				String description = TextUtils.isEmpty(objectName) ?
					String.format(getString(R.string.share_thread_text), threadSubject, forumTitle) :
					String.format(getString(R.string.share_thread_game_text), threadSubject, forumTitle, objectName);
				String link = ActivityUtils.createBggUri("thread", threadId).toString();
				ActivityUtils.share(this, getString(R.string.share_thread_subject), description + "\n\n" + link, R.string.title_share);
				String contentName = TextUtils.isEmpty(objectName) ?
					String.format("%s | %s", forumTitle, threadSubject) :
					String.format("%s | %s | %s", objectName, forumTitle, threadSubject);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
