package land;

public class Entity {
	public World world;
	
	public float x, y, z;
	public float rx, ry;
	
	public Entity(World world, float x, float y, float z) {
		this.world = world;
		move(x, y, z);
	}
	
	public void move(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void rotate(float rx, float ry) {
		this.rx = rx;
		this.ry = ry;
	}
}