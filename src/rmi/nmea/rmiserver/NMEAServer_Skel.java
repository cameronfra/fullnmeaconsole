// Skeleton class generated by rmic, do not edit.
// Contents subject to change without notice.

package rmi.nmea.rmiserver;

public final class NMEAServer_Skel
    implements java.rmi.server.Skeleton
{
    private static final java.rmi.server.Operation[] operations = {
	new java.rmi.server.Operation("nmea.server.ctx.NMEADataCache getNMEACache()"),
	new java.rmi.server.Operation("void registerForNotification(rmi.nmea.client.Notifiable)"),
	new java.rmi.server.Operation("void unregisterForNotification(rmi.nmea.client.Notifiable)")
    };
    
    private static final long interfaceHash = 1702952686599859746L;
    
    public java.rmi.server.Operation[] getOperations() {
	return (java.rmi.server.Operation[]) operations.clone();
    }
    
    public void dispatch(java.rmi.Remote obj, java.rmi.server.RemoteCall call, int opnum, long hash)
	throws java.lang.Exception
    {
	if (opnum < 0) {
	    if (hash == 3544281633149487421L) {
		opnum = 0;
	    } else if (hash == -4373675191714245596L) {
		opnum = 1;
	    } else if (hash == 5396830962268796561L) {
		opnum = 2;
	    } else {
		throw new java.rmi.UnmarshalException("invalid method hash");
	    }
	} else {
	    if (hash != interfaceHash)
		throw new java.rmi.server.SkeletonMismatchException("interface hash mismatch");
	}
	
	rmi.nmea.rmiserver.NMEAServer server = (rmi.nmea.rmiserver.NMEAServer) obj;
	switch (opnum) {
	case 0: // getNMEACache()
	{
	    call.releaseInputStream();
	    nmea.server.ctx.NMEADataCache $result = server.getNMEACache();
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 1: // registerForNotification(Notifiable)
	{
	    rmi.nmea.client.Notifiable $param_Notifiable_1;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_Notifiable_1 = (rmi.nmea.client.Notifiable) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    server.registerForNotification($param_Notifiable_1);
	    try {
		call.getResultStream(true);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 2: // unregisterForNotification(Notifiable)
	{
	    rmi.nmea.client.Notifiable $param_Notifiable_1;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_Notifiable_1 = (rmi.nmea.client.Notifiable) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    server.unregisterForNotification($param_Notifiable_1);
	    try {
		call.getResultStream(true);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	default:
	    throw new java.rmi.UnmarshalException("invalid method number");
	}
    }
}
