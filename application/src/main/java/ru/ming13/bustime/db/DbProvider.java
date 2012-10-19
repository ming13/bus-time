package ru.ming13.bustime.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import ru.ming13.bustime.db.content.DbImporter;
import ru.ming13.bustime.db.model.Routes;
import ru.ming13.bustime.db.model.Stations;
import ru.ming13.bustime.db.sqlite.DbOpenHelper;


public class DbProvider
{
	private static class AlreadyInstantiatedException extends RuntimeException
	{
	}

	private DbOpenHelper databaseOpenHelper;

	private Routes routes;
	private Stations stations;

	public static DbProvider getInstance() {
		return DbProviderHolder.instance;
	}

	private static class DbProviderHolder
	{
		private static DbProvider instance;

		public DbProviderHolder(Context context) {
			instance = new DbProvider(context);
		}
	}

	private DbProvider(Context context) {
		if (getInstance() != null) {
			throw new AlreadyInstantiatedException();
		}

		importDatabaseIfNecessary(context);
		databaseOpenHelper = new DbOpenHelper(context.getApplicationContext());
	}

	private void importDatabaseIfNecessary(Context context) {
		DbImporter dbImporter = new DbImporter(context);

		if (!dbImporter.isLocalDatabaseExist()) {
			dbImporter.importFromAssets();
		}
	}

	public static DbProvider getInstance(Context context) {
		if (getInstance() == null) {
			new DbProviderHolder(context);
		}

		return getInstance();
	}

	public void refreshDatabase(Context context) {
		databaseOpenHelper.close();

		databaseOpenHelper = new DbOpenHelper(context);

		routes = new Routes();
		stations = new Stations();
	}

	public SQLiteDatabase getDatabase() {
		return databaseOpenHelper.getReadableDatabase();
	}

	public Routes getRoutes() {
		if (routes == null) {
			routes = new Routes();
		}

		return routes;
	}

	public Stations getStations() {
		if (stations == null) {
			stations = new Stations();
		}

		return stations;
	}
}
