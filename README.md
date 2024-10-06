# Bohnenspiel AI - Java Implementation

## Overview

This project involves implementing an AI to play the game **Bohnenspiel** using a Monte-Carlo Tree Search (MCTS) algorithm. The AI must compete against a reference AI, which employs a Min-Max algorithm with a depth of 4. The goal is to create a smarter AI that can outperform the reference AI consistently, making decisions within a 3-second timeframe for each move.

## Game Rules - Specific Variant

In this variant of the Bohnenspiel:

1. **Capture Conditions**: 
   - A player captures beans if the last bean lands in a field with **2, 4, or 6 beans**. 
   - The player also captures beans from the previous field if it has **2, 4, or 6 beans**.
   - This process continues until a field doesn't meet the capture condition.
   
2. **Distribution of Beans**: 
   - Each player has 6 fields on their side.
   - On a player's turn, they pick a field, and beans from that field are distributed counter-clockwise across subsequent fields.
   - Beans are **not placed** in the large pits at the ends of the board, which only serve to store captured beans.

## AI Implementation

### Key Criteria

1. **AI Capability**:
   - The AI must beat a reference Min-Max AI, with its search depth limited to 4.
   - The AI should be able to make decisions within 3 seconds per turn.
   - An MCTS-based implementation with Light Playouts should outperform the reference AI significantly.

2. **Evaluation Against Reference AI**:
   - The AI’s strength will be measured against the reference AI in scheduled periods, as announced by the course leader.
   - The AI must demonstrate strong performance and consistently make decisions within the given time limit.

### Implementation Details

#### REST API Integration

The AI must use the REST interface provided at [Bohnenspiel API Documentation](http://bohnenspiel.informatik.uni-mannheim.de/). This interface allows the AI to play against the reference AI and other players.

- **Java Integration**:
  - A basic random AI is available in the [Main.java](http://bohnenspiel.informatik.uni-mannheim.de/doc/index) class. This class can be used to connect the custom AI to the game interface.

- **Python Integration**:
  - For those implementing in Python, it is recommended to create a counterpart to `Main.java`. Python offers many libraries to interface with REST APIs, such as those described in [Real Python’s API Integration Guide](https://realpython.com/api-integration-in-python/).

### AI Strategy

The AI will use a **Monte-Carlo Tree Search (MCTS)** algorithm to decide moves. Key aspects include:

1. **Tree Search Algorithm**: 
   - The AI will simulate potential moves, evaluating future game states by performing playouts (random simulations) from each possible move.

2. **Light Playouts**: 
   - To improve efficiency, "light" playouts will be used. These playouts use simple heuristic-based simulations rather than detailed look-ahead logic to save computation time.

3. **Heuristic Evaluation**: 
   - The AI will use a heuristic function to evaluate game states. This will include factors such as:
     - The number of beans captured.
     - Potential to set up further captures on subsequent turns.
     - Blocking the opponent’s ability to capture beans.

4. **Search Depth**: 
   - While the reference AI is limited to a depth of 4, the MCTS implementation can effectively evaluate much deeper strategies through playouts, thus outperforming the reference AI.
   
### Performance and Decision Time

- **Time Constraints**: The AI must make decisions within 3 seconds per move.
- **Depth of Analysis**: Even though the reference AI has a maximum depth of 4, the MCTS algorithm can simulate thousands of potential game states within the allowed time, leading to stronger decision-making.

## How to Run the AI

### Java Implementation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/bohnenspiel-ai
   cd bohnenspiel-ai
