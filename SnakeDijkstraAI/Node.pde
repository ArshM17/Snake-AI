class Node{
	boolean isObstacle;
	int x;
	int y;
	float g;
	float h;
	Node parent;
	
	Node(int x, int y, boolean isObstacle){
		this.isObstacle = isObstacle;
		this.x = x;
		this.y = y;
		this.g = 0f;
		this.h = 0f;
        this.parent = null;
	}
}