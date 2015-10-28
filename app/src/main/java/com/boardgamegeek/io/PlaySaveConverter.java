package com.boardgamegeek.io;

import android.text.TextUtils;

import com.boardgamegeek.model.PlayPostResponse;
import com.google.gson.Gson;

import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;

public class PlaySaveConverter extends PostConverter {
	private final GsonConverter converter;

	public PlaySaveConverter() {
		converter = new GsonConverter(new Gson());
	}

	@Override
	public Object fromBody(TypedInput body, Type type) throws ConversionException {
		markBody(body);
		try {
			return converter.fromBody(body, type);
		} catch (ConversionException e) {
			// we didn't get the expected JSON
			String content = getContent(body);
			if (typeIsExpected(type)) {
				String errorMessage = extractErrorMessage(content);
				if (!TextUtils.isEmpty(errorMessage)) {
					return new PlayPostResponse(errorMessage);
				}
			}
			throw new ConversionException(content, e);
		}
	}
}
