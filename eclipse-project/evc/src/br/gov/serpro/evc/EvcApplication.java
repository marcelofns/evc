/**
 * Expresso Video Chat Red5 application
 * 
 *  Author: Serpro
 */

package br.gov.serpro.evc;

import java.util.Set;

import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.server.api.service.IServiceCapableConnection;
import org.red5.server.api.stream.IStreamAwareScopeHandler;

public final class EvcApplication extends ApplicationAdapter implements IPendingServiceCallback,
	IStreamAwareScopeHandler{
	
	public static final String ATT_USER_ID = "userId";
	private IScope appScope;
	
	@Override
	public boolean appStart(IScope app) {
		
		this.appScope = app;
		
		return super.appStart(app);
	}

	@Override
	public boolean appConnect(IConnection con, Object[] params) {
		super.appConnect(con, params);

		try{
			String id = EvcUser.buildId();
			con.getClient().setAttribute(ATT_USER_ID, id);
			
			IServiceCapableConnection service = (IServiceCapableConnection)con;
			service.invoke("setId", new Object[]{id}, this);
		}
		catch(Exception e){
			return false;
		}
		
		return true;
	}
	
	/**
	 * Get the user connection by id
	 * @param id
	 * @return User connection
	 */
	private IConnection getUser(String id) {
		for (Set<IConnection> set : appScope.getConnections()) {
			   for (IConnection con : set) {
			      if(con.getClient().getAttribute(ATT_USER_ID).equals(id))
			    	  return con;
			   }
			}
		
		return null;
	}
	
	public boolean connectToUser(String id, String otherUserId) {
		IConnection otherUser = getUser(otherUserId);
		if(otherUser != null) {
			sendPeerConnect(otherUser, id);
			return true;
		}
		
		return false;
	}
	
	private void sendPeerConnect(IConnection user, String otherUserId) {
		IServiceCapableConnection service = (IServiceCapableConnection)user;
		
		service.invoke("onPeerConnect", new Object[]{otherUserId}, this);
	}
	
	/**
	 * Relay method is used to send some messages to remote peer through the server.
	 * Implemented to work the same as Cumulus.
	 * 
	 * @param id - The remote id to send message
	 * @param action - The action name
	 * @param username - The sender username
	 * @return
	 */
	public boolean relay(String id, String action, String username){
		String myId= null;
		IConnection me = null;
		try{
			me = Red5.getConnectionLocal();
			myId = (String) me.getClient().getAttribute(ATT_USER_ID);
		}
		catch(Exception e){
			return false;
		}
		
		IConnection user = getUser(id);
		
		
		if(user != null) {
			IServiceCapableConnection service = (IServiceCapableConnection)user;
			
			service.invoke("onRelay", new Object[]{myId, action , username}, this);
			return true;
		}
		else { // remote user not found
			if(action.equals("invite")){
				// send back reject as response
				
				IServiceCapableConnection service = (IServiceCapableConnection)me;
				service.invoke("onRelay", new Object[]{id, "reject" , username}, this);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void resultReceived(IPendingServiceCall arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
}
