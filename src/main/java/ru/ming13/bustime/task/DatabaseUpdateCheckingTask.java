package ru.ming13.bustime.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;

import ru.ming13.bustime.backend.DatabaseBackend;
import ru.ming13.bustime.bus.BusEvent;
import ru.ming13.bustime.bus.BusProvider;
import ru.ming13.bustime.bus.DatabaseUpdateAvailableEvent;
import ru.ming13.bustime.bus.DatabaseUpdateNotAvailableEvent;
import ru.ming13.bustime.util.Preferences;

public class DatabaseUpdateCheckingTask extends AsyncTask<Void, Void, BusEvent>
{
	private final Context context;

	public static void execute(@NonNull Context context) {
		new DatabaseUpdateCheckingTask(context).execute();
	}

	private DatabaseUpdateCheckingTask(Context context) {
		this.context = context.getApplicationContext();
	}

	@Override
	protected BusEvent doInBackground(Void... parameters) {
		String localDatabaseVersion = getLocalDatabaseVersion();
		String serverDatabaseVersion = getServerDatabaseVersion();

		if (StringUtils.isBlank(serverDatabaseVersion)) {
			return new DatabaseUpdateNotAvailableEvent();
		}

		if (StringUtils.isBlank(localDatabaseVersion)) {
			return new DatabaseUpdateAvailableEvent();
		}

		if (!localDatabaseVersion.equals(serverDatabaseVersion)) {
			return new DatabaseUpdateAvailableEvent();
		}

		return new DatabaseUpdateNotAvailableEvent();
	}

	private String getLocalDatabaseVersion() {
		return Preferences.of(context).getDatabaseVersion();
	}

	private String getServerDatabaseVersion() {
		return DatabaseBackend.with(context).getDatabaseVersion();
	}

	@Override
	protected void onPostExecute(BusEvent busEvent) {
		super.onPostExecute(busEvent);

		BusProvider.getBus().post(busEvent);
	}
}
