package app.android.bustime.ui;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public abstract class FormActivity extends Activity
{
	protected final Context activityContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initializeConfirmButton();
	}

	protected void initializeConfirmButton() {
		getConfirmButton().setOnClickListener(confirmListener);
	}

	protected abstract Button getConfirmButton();

	protected final View.OnClickListener confirmListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view) {
			readUserDataFromFields();

			String userDataErrorMessage = getUserDataErrorMessage();

			if (userDataErrorMessage.isEmpty()) {
				performSubmitAction();
			} else {
				UserAlerter.alert(activityContext, userDataErrorMessage);
			}
		}
	};

	protected abstract void readUserDataFromFields();

	protected abstract String getUserDataErrorMessage();

	protected abstract void performSubmitAction();
}
