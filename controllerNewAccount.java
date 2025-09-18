package guiNewAccount;

import java.sql.SQLException;

import database.Database; import entityClasses.User; import validation.UserNameRecognizer; import validation.PasswordPolicy; // only if you used the helper class import javafx.scene.control.Alert; // for popups

public class ControllerNewAccount {

/*-********************************************************************************************

The User Interface Actions for this page

This controller is not a class that gets instantiated.  Rather, it is a collection of protected
static methods that can be called by the View (which is a singleton instantiated object) and 
the Model is often just a stub, or will be a singleton instantiated object.

*/


// Reference for the in-memory database so this package has access
private static Database theDatabase = applicationMain.FoundationsMain.database;

/**********
 * <p> Method: public doCreateUser() </p>
 * 
 * <p> Description: This method is called when the user has clicked on the User Setup
 * button.  This method checks the input fields to see that they are valid.  If so, it then
 * creates the account by adding information to the database.
 * 
 * The method reaches batch to the view page and to fetch the information needed rather than
 * passing that information as parameters.
 * 
 */	
protected static void doCreateUser() {
	
	// Fetch the username and password. (We use the first of the two here, but we will validate
	// that the two password fields are the same before we do anything with it.)
	String username = ViewNewAccount.text_Username.getText();
	String password = ViewNewAccount.text_Password1.getText();
	
	// Display key information to the log
	System.out.println("** Account for Username: " + username + "; theInvitationCode: "+
			ViewNewAccount.theInvitationCode + "; email address: " + 
			ViewNewAccount.emailAddress + "; Role: " + ViewNewAccount.theRole);
	
	// ====== Task 7: Validate the USERNAME FIRST ======
	String uErr = UserNameRecognizer.checkForValidUserName(username);
	if (!uErr.isEmpty()) {
		showError("Username error", uErr.trim());
		return;
	}
	// ================================================

	// Initialize local variables that will be created during this process
	int roleCode = 0;
	User user = null;

	// Make sure the two passwords are the same.	
	if (ViewNewAccount.text_Password1.getText().
			compareTo(ViewNewAccount.text_Password2.getText()) == 0) {
		
		// ====== Task 9: Validate the PASSWORD (max 32, has upper/lower/digit/special) ======
		String pErr = PasswordPolicy.validate(password);   // uses validation.PasswordPolicy
		if (!pErr.isEmpty()) {
			showError("Password error", pErr);
			// (optional) clear fields to prompt re-entry
			// ViewNewAccount.text_Password1.setText("");
			// ViewNewAccount.text_Password2.setText("");
			return;
		}
		// ===============================================================================

		// The passwords match so we will set up the role and the User object base on the 
		// information provided in the invitation
		if (ViewNewAccount.theRole.compareTo("Admin") == 0) {
			roleCode = 1;
			user = new User(username, password, "", "", "", "", "", true, false, false);
		} else if (ViewNewAccount.theRole.compareTo("Role1") == 0) {
			roleCode = 2;
			user = new User(username, password, "", "", "", "", "", false, true, false);
		} else if (ViewNewAccount.theRole.compareTo("Role2") == 0) {
			roleCode = 3;
			user = new User(username, password, "", "", "", "", "", false, false, true);
		} else {
			System.out.println(
					"**** Trying to create a New Account for a role that does not exist!");
			System.exit(0);
		}
		
		// Unlike the FirstAdmin, we know the email address, so set that into the user as well.
		user.setEmailAddress(ViewNewAccount.emailAddress);

		// Inform the system about which role will be played
		applicationMain.FoundationsMain.activeHomePage = roleCode;
		
		// Create the account based on user and proceed to the user account update page
		try {
			// Create a new User object with the pre-set role and register in the database
			theDatabase.register(user);
		} catch (SQLException e) {
			System.err.println("*** ERROR *** Database error: " + e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		
		// The account has been set, so remove the invitation from the system
		theDatabase.removeInvitationAfterUse(
				ViewNewAccount.text_Invitation.getText());
		
		// Set the database so it has this user and the current user
		theDatabase.getUserAccountDetails(username);

		// Navigate to the Welcome Login Page
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewNewAccount.theStage, user);
	}
	else {
		// The two passwords are NOT the same, so clear the passwords, explain the passwords
		// must be the same, and clear the message as soon as the first character is typed.
		ViewNewAccount.text_Password1.setText("");
		ViewNewAccount.text_Password2.setText("");
		ViewNewAccount.alertUsernamePasswordError.showAndWait();
	}
}

protected static void performQuit() {
	System.out.println("Perform Quit");
	System.exit(0);
}
/**********
 * <p> Method: public performQuit() </p>
 * 
 * <p> Description: This method is called when the user has clicked on the Quit button.  Doing
 * this terminates the execution of the application.  All important data must be stored in the
 * database, so there is no cleanup required.  (This is important so we can minimize the impact
 * of crashed.)
 * 
 */	
private static void showError(String title, String message) {
	Alert a = new Alert(Alert.AlertType.ERROR);
	a.setTitle(title);
	a.setHeaderText(null);
	a.setContentText(message);
	a.showAndWait();
}
}
