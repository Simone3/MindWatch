package it.polimi.aui.common.communication;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;

/**
 * Helper class that sends a message from phone to wear or vice-versa in a separate thread
 */
public class SendMessageToBestNode extends Thread
{
    private String path;
    private String serializedObject;
    private GoogleApiClient apiClient;

    public SendMessageToBestNode(GoogleApiClient apiClient, String path, Serializable object)
    {
        this.path = path;
        this.apiClient = apiClient;

        Gson gson = new Gson();
        this.serializedObject = gson.toJson(object);
    }

    @Override
    public void run()
    {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(apiClient).await();
        String bestNodeId = pickBestNodeId(nodes.getNodes());

        if(bestNodeId!=null)
        {
            Wearable.MessageApi.sendMessage(apiClient, bestNodeId, path, serializedObject.getBytes()).await();
        }
    }

    /**
     * Helper to pick the best node (closest connected device)
     * @param nodes all available nodes
     * @return the ID of the best node (null if "nodes" is empty)
     */
    private String pickBestNodeId(List<Node> nodes)
    {
        String bestNodeId = null;

        // Find a nearby node or pick one arbitrarily
        for(Node node : nodes)
        {
            if(node.isNearby())
            {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }
}
