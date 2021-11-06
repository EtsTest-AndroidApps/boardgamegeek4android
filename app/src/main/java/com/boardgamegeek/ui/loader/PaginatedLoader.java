package com.boardgamegeek.ui.loader;

import android.content.Context;

import com.boardgamegeek.ui.model.PaginatedData;

import androidx.loader.content.AsyncTaskLoader;

public abstract class PaginatedLoader<T> extends AsyncTaskLoader<PaginatedData<T>> {
	private PaginatedData<T> data;
	private boolean isLoading;

	public PaginatedLoader(Context context) {
		super(context);
		isLoading = true;
		data = null;
	}

	@Override
	protected void onStartLoading() {
		if (data != null) {
			deliverResult(data);
		}
		if (takeContentChanged() || data == null) {
			forceLoad();
		}
	}

	@Override
	public PaginatedData<T> loadInBackground() {
		isLoading = true;
		return fetchPage(getNextPageNumber());
	}

	protected int getNextPageNumber() {
		return data == null ? 1 : data.getNextPageNumber();
	}

	@Override
	public void deliverResult(PaginatedData<T> data) {
		isLoading = false;
		if (data != null) {
			if (this.data == null) {
				this.data = data;
			} else if (data.getCurrentPageNumber() == this.data.getNextPageNumber()) {
				this.data.addPage(data.getItems());
			}
		}
		if (isStarted()) {
			super.deliverResult(new PaginatedData<>(this.data));
		}
	}

	@Override
	protected void onStopLoading() {
		isLoading = false;
		cancelLoad();
	}

	@Override
	protected void onReset() {
		super.onReset();
		onStopLoading();
		data = null;
	}

	public boolean isLoading() {
		return isLoading;
	}

	public boolean hasMoreResults() {
		return data != null && data.hasMoreResults();
	}

	protected abstract PaginatedData<T> fetchPage(int pageNumber);
}
