/**
 *
 */
package org.promasi.server.client.state;

import java.net.ProtocolException;

import org.apache.commons.lang.NullArgumentException;
import org.promasi.protocol.request.RequestBuilder;
import org.promasi.protocol.request.StartGameRequest;
import org.promasi.protocol.response.InternalErrorResponse;
import org.promasi.protocol.response.StartGameResponse;
import org.promasi.protocol.response.WrongProtocolResponse;
import org.promasi.server.core.AbstractClientState;
import org.promasi.server.core.ProMaSi;
import org.promasi.server.core.ProMaSiClient;
import org.promasi.server.core.game.Game;

/**
 * @author m1cRo
 *
 */
public class WaitingGameClientState extends AbstractClientState {

	/**
	 *
	 */
	private ProMaSi _promasi;

	/**
	 *
	 */
	private Game _game;

	/**
	 *
	 * @param promasi
	 * @param game
	 * @throws NullArgumentException
	 */
	public WaitingGameClientState(ProMaSi promasi,Game game)throws NullArgumentException
	{
		if(promasi==null)
		{
			throw new NullArgumentException("Wrong argument promasi==null");
		}

		if(game==null)
		{
			throw new NullArgumentException("Wrong argument game==null");
		}
		_promasi=promasi;
		_game=game;
	}

	/* (non-Javadoc)
	 * @see org.promasi.server.client.state.IClientState#onReceive(org.promasi.server.core.ProMaSiClient, java.lang.String)
	 */
	@Override
	public void onReceive(ProMaSiClient client, String recData)throws  NullArgumentException {
		if(client==null)
		{
			throw new NullArgumentException("Wrong argument client==null");
		}

		if(recData==null)
		{
			throw new NullArgumentException("Wrong argument client==null");
		}
		try
		{
			Object object=RequestBuilder.buildRequest(recData);
			if(object instanceof StartGameRequest)
			{
				changeClientState(client,new PlayingGameClientState(_promasi,_game.getGameId()));
				client.sendMessage(new StartGameResponse(_game.getGameId()).toXML());
			}
			else
			{
				client.sendMessage(new WrongProtocolResponse().toXML());
			}
		}
		catch(ProtocolException e)
		{
			client.sendMessage(new WrongProtocolResponse().toXML());
		}
		catch(NullArgumentException e)
		{
			client.sendMessage(new InternalErrorResponse().toXML());
		}
		catch(IllegalArgumentException e)
		{
			client.sendMessage(new InternalErrorResponse().toXML());
		}
	}

}