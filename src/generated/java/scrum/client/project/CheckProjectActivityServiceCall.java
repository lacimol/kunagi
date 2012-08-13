// // ----------> GENERATED FILE - DON'T TOUCH! <----------

package scrum.client.project;

public class CheckProjectActivityServiceCall extends scrum.client.core.AServiceCall {

    public  CheckProjectActivityServiceCall() {
    }

    public void execute(Runnable returnHandler) {
        serviceCaller.onServiceCall(this);
        serviceCaller.getService().checkProjectActivity(serviceCaller.getConversationNumber(), new DefaultCallback(this, returnHandler));
    }

    @Override
    public String toString() {
        return "CheckProjectActivity";
    }

}

