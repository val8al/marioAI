package bu.marp;

import java.lang.reflect.Array;
import java.util.Arrays;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class ReactiveAgent implements Agent
{
    private String name;
    private boolean[] action;


    public ReactiveAgent()
    {
        setName("ReactiveAgent");
        reset();
    }

    //Reset the game's controller
    public void reset() 
    {
        action = new boolean[Environment.numberOfButtons];
        action[Mario.KEY_SPEED] = true;
    }

    public boolean[] getAction(Environment observation)
    {
    	//Framework methods for getting information about the environment
    	byte[][] scene = observation.getCompleteObservation(); 
    	float[] enemies = observation.getEnemiesFloatPos();
		float[] realMarioPos = observation.getMarioFloatPos();
		float marioX = realMarioPos[0];
		float marioY = realMarioPos[1];
		boolean marioCanJump = observation.mayMarioJump();
		boolean marioOnGround = observation.isMarioOnGround();
	
		boolean enemiesOnSight = enemies.length > 0;
		//Booleans that enclose the possible positions in which Mario may find a wall (-10) or a pit (-11)
		//Mario may see everything in the array groups from 10 foward, every object would eventually be at position 22
		//of each array
		boolean inmediateWall =(scene[11][21] == -10)
				||(scene[12][21] == -10)
				||(scene[13][21] == -10)
				||(scene[14][21] == -10)
				||(scene[15][21] == -10);
		boolean softWall =(scene[11][21] == -11)
				||(scene[12][21] == -11)
				||(scene[13][21] == -11)
				||(scene[14][21] == -11)
				||(scene[15][21] == -11);
		boolean coinsAhead = (scene[9][15]==34)
				||(scene[10][21]==34)
				||(scene[11][21]==34)
				||(scene[12][21]==34)
				||(scene[13][21]==34)
				||(scene[14][21]==34)
				||(scene[13][21]==34);
		
		//System.out.println("x"+Arrays.deepToString(scene)+"\n");
	
		if(!marioOnGround) { //prevent Mario from making short jumps
			action[Mario.KEY_JUMP]= true;
		}
		
		else if(inmediateWall) {//do the same for a wall
			System.out.println("wall");
			if(marioCanJump) {
				action[Mario.KEY_JUMP]= true;
			}
			else {
				action[Mario.KEY_JUMP]= false;
			}
			action[Mario.KEY_RIGHT] = true;
			
		}
		else if(enemiesOnSight) { //slow mario and jump when he sees enemies
			if(marioCanJump) {
				action[Mario.KEY_JUMP]= true;
			}
			else {
				action[Mario.KEY_JUMP]= false;
			}
			action[Mario.KEY_SPEED] = false;
			
			System.out.println("enemies"); 
		}
		else if(softWall) { // when Mario is on the border of a pit jump
			System.out.println("pit");
			if(marioCanJump) {
				action[Mario.KEY_JUMP]= true;
			}
			else {
				action[Mario.KEY_JUMP]= false;
			}
			action[Mario.KEY_RIGHT] = true;
		}
		else if(coinsAhead) { //if there are no walls, pits or enemies keep Mario on the ground
			
			action[Mario.KEY_JUMP]= false;
			action[Mario.KEY_SPEED] = true;
			//try{Thread.sleep(300);}catch (InterruptedException e) {}
			System.out.println("coins"); 
		}
		else { //reset the controller
			System.out.println("idle");
			action[Mario.KEY_JUMP]= false;
			
		}
	
		action[Mario.KEY_RIGHT] = true; //always move to the right
		
        return action;
    }


    public Agent.AGENT_TYPE getType()
    {
        return Agent.AGENT_TYPE.AI;
    }

    public String getName() {        return name;    }

    public void setName(String Name) { this.name = Name;    }

}