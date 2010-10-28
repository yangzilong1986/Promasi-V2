/**
 *
 */
package org.promasi.multiplayer.server.clientstate;

import java.net.ProtocolException;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.promasi.multiplayer.AbstractClientState;
import org.promasi.multiplayer.ProMaSi;
import org.promasi.multiplayer.ProMaSiClient;
import org.promasi.multiplayer.game.Game;
import org.promasi.multiplayer.game.GameModel;
import org.promasi.network.protocol.client.request.CreateNewGameRequest;
import org.promasi.network.protocol.client.request.RequestBuilder;
import org.promasi.network.protocol.client.response.CreateNewGameResponse;
import org.promasi.network.protocol.client.response.InternalErrorResponse;
import org.promasi.network.protocol.client.response.WrongProtocolResponse;
import org.promasi.shell.Story.StoriesPool;
import org.promasi.shell.Story.Story;

/**
 * @author m1cRo
 *
 */
public class GameMasterClientState extends AbstractClientState {

	/**
	 *
	 */
	private ProMaSi _promasi;


	/**
	 *
	 * @param promasi
	 * @param game
	 * @throws NullArgumentException
	 */
	public GameMasterClientState(ProMaSi promasi)throws NullArgumentException
	{
		if(promasi==null)
		{
			throw new NullArgumentException("Wrong argument promasi==null");
		}
		
		_promasi=promasi;
	}

	/* (non-Javadoc)
	 * @see org.promasi.server.client.state.IClientState#onReceive(org.promasi.server.core.ProMaSiClient, java.lang.String)
	 */
	@Override
	public void onReceive(ProMaSiClient client, String recData)throws NullArgumentException {
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
			if(object instanceof CreateNewGameRequest)
			{
				CreateNewGameRequest request=(CreateNewGameRequest)object;
				List<Story> stories=StoriesPool.getAllStories();
				for(Story story : stories)
				{
					if(story.getName().compareTo(request.getStoryId())==0){
						Game game=new Game(client,new GameModel(story));
						try
						{
							story.setName(request.getGameName());
							_promasi.createNewGame(request.getGameName(),game);
							client.sendMessage(new CreateNewGameResponse().toProtocolString());
							changeClientState(client,new WaitingForPlayersClientState(_promasi,game));
						}
						catch(IllegalArgumentException e)
						{
						//	//client.sendMessage(new CreateNewGameResponse(false).toProtocolString());
						}
					}
				}
			}
			else
			{
				client.sendMessage(new WrongProtocolResponse().toProtocolString());
				client.disconnect();
			}
		}
		catch(ProtocolException e)
		{
			client.sendMessage(new WrongProtocolResponse().toProtocolString());
			client.disconnect();
		}
		catch(NullArgumentException e)
		{
			client.sendMessage(new InternalErrorResponse().toProtocolString());
			client.disconnect();
		}
		catch(IllegalArgumentException e)
		{
			client.sendMessage(new InternalErrorResponse().toProtocolString());
			client.disconnect();
		}
	}

}
