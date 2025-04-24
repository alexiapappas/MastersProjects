package assignment08;

public class Node {
    int x_, y_;
    int actualCost_, estimatedCost_;
    Node previous;

    // Node constructor
    Node(int x, int y, int generalCost, int heuristicCost, Node parent){
        this.x_ = x;
        this.y_ = y;
        this.actualCost_ = generalCost;
        this.estimatedCost_ = heuristicCost;
        this.previous = parent;
    }

    int getCost(){
        return actualCost_ + estimatedCost_;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj){
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Node node = (Node) obj;
        return x_ == node.x_ && y_ == node.y_;
    }

    @Override
    public int hashCode(){
        String hash = "" + x_ + y_;
        return Integer.parseInt(hash);
    }
}
