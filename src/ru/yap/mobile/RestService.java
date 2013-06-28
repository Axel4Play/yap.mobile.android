package ru.yap.mobile;

import ru.yap.mobile.RequestFactory;
import ru.yap.mobile.ForumOperation;

import com.foxykeep.datadroid.service.RequestService;

public class RestService extends RequestService {

	@Override
	public Operation getOperationForType(int requestType) {
		switch (requestType) {
		case RequestFactory.REQUEST_FORUM:
			return new ForumOperation();
		default:
			return null;
		}
	}

}

//EOF