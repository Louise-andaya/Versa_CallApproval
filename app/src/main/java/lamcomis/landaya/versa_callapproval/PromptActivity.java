package lamcomis.landaya.versa_callapproval;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;


import java.util.HashMap;

public class PromptActivity {
    Context context;
    DatabaseHelper myDb;
    static SessionManager  sessionManager;
    static String user_type;
    public static  void showAlert(final Context context, final String alert_type){
        sessionManager = new SessionManager(context);
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_type = user.get("user_type");
        String title = "";
        String message = "";
        String yes = "Yes";
        String cancel = "No";

        switch (alert_type){
            case "error":
                title = " Volley Error";
                message = "Please Contact MIS Local(5106) for this error";
                yes = "Okay";
                cancel = "";
                break;

            case "internet":
                title = " Error";
                message = "Please Check Your Internet Connection";
                yes = "Okay";
                cancel = "";
                break;

            case "invalid_password":
                title = " Error";
                message = "Invalid Password";
                yes = "Okay";
                cancel = "";
                break;

            case "empty":
                title = "";
                message = "Empty Credentials";
                yes = "Okay";
                cancel = "";
                break;

            case "not_found":
                title = " Error";
                message = "User not Found";
                yes = "Okay";
                cancel = "";
                break;

            case "dont_match":
                title = "";
                message = "Password Dont Match";
                yes = "Okay";
                cancel = "";
                break;

            case "logout":
                title = " Alert";
                message = "Are you sure you want Logout?";
                yes = "Yes";
                cancel = "No";
                break;

            case "success":
                title = " Success";
                message = "Call Approval Process Successfully";
                yes = "Okay";
                cancel = "";
                break;

            case "success_changepass":
                title = " Success";
                message = "Password Updated Successfully";
                yes = "Okay";
                cancel = "";
                break;

            case "back":
                title = " Alert";
                message = "Are you sure you want to Exit?";
                yes = "Yes";
                cancel = "No";
                break;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title)
                .setIcon(R.drawable.versa_logo)
                .setCancelable(false)
                .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;

                        switch (alert_type){
                            case "error":
                                dialog.cancel();
                                break;

                            case "internet":
                                dialog.cancel();
                                break;

                            case "invalid_password":
                                dialog.cancel();
                                break;

                            case "empty":
                                dialog.cancel();
                                break;

                            case "dont_match":
                                dialog.cancel();
                                break;

                            case "not_found":
                                dialog.cancel();
                                break;

                            case "back":
                                intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_HOME);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(intent);
                                System.exit(0);
                                break;

                            case "success":
                                Intent intent1 = new Intent(context, UserDashBoard.class);
                                context.startActivity(intent1);
                                break;

                            case "success_changepass":
                                dialog.cancel();
                                break;

                            case "logout":
                                sessionManager = new SessionManager(context);
                                sessionManager.logoutUser();
                                Intent tohome = new Intent(context,Login.class);
                                context.startActivity(tohome);
                                break;

                        }
                    }
                });


        builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (alert_type){
                    case "error":
                        break;

                    case "back":
                        dialog.cancel();
                        break;

                    case "logout":
                        if(user_type.equals("user")){
                            Intent tohome = new Intent(context,UserDashBoard.class);
                            context.startActivity(tohome);
                        }
                        else{
                            Intent tohome = new Intent(context,AdminDashboard.class);
                            context.startActivity(tohome);
                        }
                        dialog.cancel();
                        break;



                    default: dialog.cancel();
                }


            }
            });
        builder.create().show();

    }
}
