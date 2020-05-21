package bu.marp;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import java.util.HashMap;

import java.util.Map;

public class LearningAgent implements Agent {
	private String name;
	private boolean[] action;
	private String[] keyChain = new String[9999];
	private int health = 2;
	private int kills = 0;
	private double prevPos = 0;
	private int jumpStuck = 0;

	private Map<String, Double> dictionary = new HashMap<String, Double>();

	int fileSize = 0;
	int currentRow = 0;

	// qlearning related
	private double learningStart = 0;
	private double learningRate = 0.0005;
	private final double DISCOUNT_RATE = 0.8;

	public LearningAgent() {
		setName("LearningAgent");

		reset();
	}

	// Reset the game's controller
	public void reset() {
		action = new boolean[Environment.numberOfButtons];
		action[Mario.KEY_SPEED] = true;
	}

	public boolean[] getAction(Environment observation) {

		
		action[Mario.KEY_RIGHT] = true; // always move to the right

		float marioX = Math.round(observation.getMarioFloatPos()[0]);
		float marioY = Math.round(observation.getMarioFloatPos()[1]);
		int currentHealth = observation.getMarioMode();
		int enemiesKilledRound = observation.getKillsTotal();
		boolean onGround = observation.isMarioOnGround();
		
		int qAction = 0;
		double reward = 0;
		boolean learnLock = false;
	

		String dictKey = Float.toString(marioX) + Float.toString(qAction);
		keyChain[currentRow] = dictKey;

	
		if(prevPos == marioX) {//stuck mario handling
			reward -= 10;
			jumpStuck ++;
			learnLock = true;
			//System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxx"+jumpStuck+"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			explore(marioX,onGround,qAction,reward);
		}else {
			prevPos = marioX;
		}
		
		// Add reward this round for health loss
		if (health > currentHealth  ) {
			health = currentHealth;
			reward -= 40;
		
		}
		// add reward this round for enemieskilled
		if (kills < enemiesKilledRound) {
			kills = enemiesKilledRound;
			reward += 5;
		
		}
		// add reward this round for enemieskilled
		

		// When mario dies reset him, add reward and apply qfunction to the dictionary
		if (Mario.status == Mario.STATUS_DEAD) {

			this.currentRow++;
			dictionary.put(dictKey, reward - 500.0);
			keyChain[currentRow] = dictKey;
			qFunction(currentRow,reward -500);
			this.currentRow = 0;
			Mario.status = 2;
			health = 2;
			kills = 0;
			System.out.println("\n\n========================== D E A D ============================\\n\\n");
			// System.out.println(dictionary.toString());
			return action;
		}

		// When mario wins reset, add reward and apply qfunction to the dictionary
		if (Mario.status == Mario.STATUS_WIN) {
			System.out
					.println("\n\n========================== W E     D I D    I T ============================\\n\\n");
			this.currentRow++;
			keyChain[currentRow] = dictKey;
			qFunction(currentRow,reward +3000);
			dictionary.put(dictKey,3000.0);
			this.currentRow = 0;
			Mario.status = 2;
			return action;
		}

		// exploration phase
		if ((Math.random()) > this.learningStart || learnLock) {
			explore(marioX,onGround,qAction,reward);
		} else { // if on threshhold, run the apply learnt from table instead
			applyLearning(observation, dictKey, marioX,marioY,qAction,reward);
		}
		// System.out.println(dictKey +" "+ reward);
		this.currentRow++;
		dictionary.putIfAbsent(dictKey, reward);


		return action;
	}

	

	public void qFunction(int index, double reward) {
		
		for (int i = 0; i < index-1; i++) {
			String nextKey = keyChain[i + 1];
			
			
			if (nextKey != null) {
				String stateAction = nextKey.substring(nextKey.length() - 1, nextKey.length());
				String altKey;
				boolean didntJump = stateAction.equals("0"); // create a new key containing the same position but
																// different action
				if (didntJump) {
					altKey = nextKey.substring(0, nextKey.length() - 1) + "1";
				} else {
					altKey = nextKey.substring(0, nextKey.length() - 1) + "0";
				}

				if (dictionary.containsKey(nextKey) && dictionary.containsKey(keyChain[i])) {
				
					if (dictionary.containsKey(altKey)) {
						double qOutput = (dictionary.get(keyChain[i])
								+ DISCOUNT_RATE * Math.max(dictionary.get(nextKey), dictionary.get(altKey)));
						dictionary.put(keyChain[i], qOutput);
						System.out.println(qOutput+"QFunction Output!!!!");

					} else {
						if(didntJump) {
							dictionary.put(altKey, 0.0);
							if (didntJump) { // then the altkey was jump
								action[Mario.KEY_JUMP] = true;
							} else {
								
								action[Mario.KEY_JUMP] = false;
							}
						}						
					}
				}else {
					dictionary.put(nextKey, 0.0);
					if (didntJump) { // then the altkey was jump
						action[Mario.KEY_JUMP] = false;
					} else {
						
						action[Mario.KEY_JUMP] = true;
					}
					
				}
			}
			//System.out.println("Llave de estado/accion:  "+keyChain[i] + "   Recompensa:   "+ dictionary.get(keyChain[i]) );
			
		}
	}
	public void explore(float marioX, boolean onGround, float qAction, double reward) {
		String dictKey ;
		if ((Math.random()) * 10 > 5) { // coinflip so it jumps or not
			
			//System.out.println("EXPLORING!!");
			action[Mario.KEY_JUMP] = true;// If mario presses the key then register it to the dictionary
			qAction = 1;
			dictKey = Float.toString(marioX) + Float.toString(qAction);

		} else {
			action[Mario.KEY_JUMP] = false;
			qAction = 0;
			dictKey = Float.toString(marioX) +onGround + Float.toString(qAction);
		}
		dictionary.putIfAbsent(dictKey, reward);
		keyChain[currentRow] = dictKey;
		this.learningStart += this.learningRate;
	}

	public void applyLearning(Environment observation, String key,float marioX, float marioY, float qAction, double reward) {

		String stateAction = key.substring(key.length() - 1, key.length());
		String altKey;
		boolean didntJump = stateAction.equals("0"); // create a new key containing the same position but different
		boolean mayJump = observation.mayMarioJump();
		
		if (didntJump) {
			altKey = key.substring(0, key.length() - 1) + "1";
		} else {
			altKey = key.substring(0, key.length() - 1) + "0";
		}
		// if mario tried both, chose the one that gave the highest reward
		if (dictionary.containsKey(key) && dictionary.containsKey(altKey)) {
			
			System.out.println(
			key+":     "+dictionary.get(key)+"    ..vs.."+altKey+":     "+dictionary.get(altKey) );
			
			if (dictionary.get(key) > dictionary.get(altKey)) {
				if (didntJump) {
					//System.out.println("NO");
					action[Mario.KEY_JUMP] = false;
				} else {
					//System.out.println("YES");
					action[Mario.KEY_JUMP] = true;
				}
				dictionary.put(key, reward);
			}
			
			else { // when the altkey is better...
				if (didntJump ) { // then the altkey was jump
					//System.out.println("YES");
					action[Mario.KEY_JUMP] = true;
				} else {
					//System.out.println("NO");
					action[Mario.KEY_JUMP] = false;
				}
				dictionary.put(key, reward);
			}
		}  else if(dictionary.containsKey(key)) { //hacemos la acción de la altkey
			if (!didntJump ) { // then the altkey was jump
				action[Mario.KEY_JUMP] = true;
			} else {
				action[Mario.KEY_JUMP] = false;
			}
			dictionary.put(altKey, reward);
			keyChain[currentRow]=altKey;
		
		
		//explore(marioX,marioY,qAction,reward);
			
			//
			//System.out.println("just learnt key!! " + altKey + "|size: " + dictionary.size());
		}

		// System.out.println(key+" key");
		// System.out.println(altKey+" alt");

	}

	public Agent.AGENT_TYPE getType() {
		return Agent.AGENT_TYPE.AI;
	}

	public String getName() {
		return name;
	}

	public void setName(String Name) {
		this.name = Name;
	}

}
