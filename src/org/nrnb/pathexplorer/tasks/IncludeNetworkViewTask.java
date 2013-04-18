package org.nrnb.pathexplorer.tasks;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.nrnb.pathexplorer.logic.FindAllPaths;
import org.nrnb.pathexplorer.logic.TableHandler;

public class IncludeNetworkViewTask extends AbstractNetworkViewTask {

	private CySwingAppAdapter adapter;
	CyNetwork net;
	List<CyNode> allNodes;
	CyNetworkView netView;
	
	public IncludeNetworkViewTask(CyNetworkView netView, CySwingAppAdapter adapter)
	{
		super(netView);	
		this.adapter = adapter;
		net = netView.getModel();
		allNodes = new ArrayList<CyNode>();
		allNodes = net.getNodeList();
		this.netView = netView;
	}
	
	public void run(TaskMonitor tm) throws Exception {
		
		CyRow row;
		Boolean isExcluded;
		for(CyNode currNode : allNodes)
		{
			row = TableHandler.defaultNodeTable.getRow(currNode.getSUID());
			isExcluded = (Boolean) row.get(TableHandler.EXCLUDED_COL,
					Boolean.class);
			if(isExcluded)
			{
				//include it
				row.set(TableHandler.EXCLUDED_COL, false);
				// clear node override
				netView.getNodeView(currNode).clearValueLock(
					BasicVisualLexicon.NODE_BORDER_WIDTH);
				netView.getNodeView(currNode).clearValueLock(
						BasicVisualLexicon.NODE_TRANSPARENCY);
			}
			//Note: visual style will be refreshed by FindAllPaths below...
		}

		// Then rerun last FindPaths call or clear path if excluded node =
		// source or target node from last FindPaths call
		ClearPathsTask refresher = new ClearPathsTask(netView, adapter);
		refresher.run(tm);
		
		if(!FindPathsNodeViewTask.nodeView.equals(null) && (Boolean)TableHandler.hiddenNodeTable.
				getRow(FindPathsNodeViewTask.nodeView.getModel().getSUID()).get(TableHandler.IN_PATH_COL,Boolean.class))
		{
			FindAllPaths pathsFinder = new FindAllPaths(FindPathsNodeViewTask.netView , FindPathsNodeViewTask.nodeView.getModel(), adapter);
			pathsFinder.findAllPathsMethod(FindPathsNodeViewTask.direction);
		}
	}
}
