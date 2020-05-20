package ch.idsia.scenarios;

import bu.marp.StrollingAgent;
import bu.marp.LearningAgent;
import bu.marp.ReactiveAgent;
import competition.cig.robinbaumgarten.AStarAgent;
import competition.cig.sergeykarakovskiy.SergeyKarakovskiy_JumpingAgent;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.AgentsPool;
import ch.idsia.ai.agents.human.HumanKeyboardAgent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 5, 2009
 * Time: 12:46:43 PM
 */
public class Play {

    public static void main(String[] args) {
        Agent controller = new LearningAgent();
        if (args.length > 0) {
            controller = AgentsPool.load (args[0]);
            AgentsPool.addAgent(controller);
        }
        EvaluationOptions options = new CmdLineOptions(new String[0]);
        
        Task task = new ProgressTask(options);
        options.setMaxFPS(false);
        options.setVisualization(true);
        options.setNumberOfTrials(10);
        options.setLevelRandSeed(1);//19,15,18
        //options.setLevelRandSeed((int) (Math.random () * Integer.MAX_VALUE));
        options.setLevelDifficulty(3);
        options.setExitProgramWhenFinished(false);
    	options.setAgent(controller);
    	
        task.setOptions(options);
        
        while(true) {
        	try {
	        task.evaluate (controller);
        	}
        	catch(IllegalArgumentException e){
        		System.out.println("goteem");
        	}
        }
    }
}
 