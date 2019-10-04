import java.util.Random;


class Node{
	private char c;
	private Node next;

	public Node(char c) {
		this.c = c;
	}

	public void setNext(Node node) {
		this.next = node;
	}

	public Node getNext() {
		return this.next;
	}

	public char getChar() {
		return this.c;
	}
}



class LinkedList{
	private Node head;
	//The update to size should be visible to every threads. 
	volatile private int size = 3;

	public void set_head(Node node) {
		this.head = node;

	}
	public Node get_head() {
		return this.head;
	}
	//size checker to confirm that the list is complete
	public int get_size() {
		return this.size;
	}
	//Get the position of a given node
	public int get_Position(Node node) {
		Node temp = this.head;
		int pos = 0;
		for(int i = 0; i < size; i++) {
			if(temp.equals(node)) {
				return pos;
			}
			temp = temp.getNext();
			pos++;

		}
		return size;
	}

	//Add a node at the location
	public void add(Node node, int position) {		
		//Create a temp node representing the start of the linked list
		Node temp = this.head;
		Node new_node = node;

		//Traverse through the list until at the desired insert position
		for(int i = 0; i < size; i++) {
			//If at the desired position, modify the linked nodes of the prev and next
			if(i == position) {
				node.setNext(temp.getNext());
				temp.setNext(node);
				size ++;
			}
			//Keep traversing 
			temp = temp.getNext();
		}




	}

	//Delete the node at the desired position
	public void delete(int position) {
		Node temp = this.head;
		for(int i = 0; i < size; i++) {
			if(i + 1 == position) {
				//if the char that we are deleting is either a b or c we will stop
				if(temp.getNext().getChar() == 'A' || temp.getNext().getChar() == 'B' || temp.getNext().getChar() == 'C') {
					break;
				}
				//only redirect the prev node's pointer to the next node
				//to ensure that we won't be accessing a null node
				//when we do Node.getNext() for any node
				temp.setNext(temp.getNext().getNext());
				size --;
			}
			temp = temp.getNext();
		}
	}

	//Print out the entire list
	public String printList() {
		String str = "";
		Node temp = this.head;
		for(int i = 0; i < size; i++) {
			//System.out.println("The char is: " + temp.getChar());
			str += temp.getChar();
			temp = temp.getNext();
		}
		System.out.println("\nThe full list is: ");
		return str;
	}
}


public class q2 {
	static volatile LinkedList list = new LinkedList();

	static boolean flag = true;
	//Generate a random character besides A B C
	public static char randomChar() {
		int min = 65;
		int max = 90;
		int num = 0;

		Random r = new Random();
		num = r.nextInt((max - min) + 1) + min;

		while(num == 65 || num == 66 || num == 67) {
			num = r.nextInt((max - min) + 1) + min;
		}

		return (char)num;

	}

	public static void main(String[] args) {
		//create the nodes
		Node a = new Node('A');
		Node b = new Node('B');
		Node c = new Node('C');

		//set the nodes to be circular and single linked 
		a.setNext(b);
		b.setNext(c);
		c.setNext(a);

		//create the linkedlist
		list.set_head(a);

		//t1Pos = list.get_head();
		//t2Pos = list.get_head();

		Thread printNode = new Thread(new T0());
		Thread addNode = new Thread(new T2());
		Thread deleteNode = new Thread(new T1());


		long start = System.currentTimeMillis();
		long end = start + 5000;	
		Thread[] thread_pool = new Thread[3];

		thread_pool[0] = printNode;
		thread_pool[1] = deleteNode;
		thread_pool[2] = addNode;

		for(int i = 0; i < 3; i++) {
			thread_pool[i].start();
		}

		while(System.currentTimeMillis() < end); //spin
		q2.flag = false;
		for (Thread thread : thread_pool) {
			try {
				thread.join();
			}
			catch(InterruptedException e){
				System.out.println("error + " + e);
			}
		}


		/*
		while (System.currentTimeMillis() < end) {
			printNode.run();
			deleteNode.run();
			addNode.run();
		}*/

		//System.out.println("START");
		System.out.println(list.printList());


	}

	static class T0 implements Runnable{
		@Override
		public void run() {
			Node t0Pos = list.get_head();
			while(q2.flag == true) {
				//Node node = q2.list.get_head();
				System.out.print(t0Pos.getChar()+ " ");
				t0Pos = t0Pos.getNext();
				//Try to sleep
				try {
					Thread.sleep(100);
				}
				catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}

	static class T1 implements Runnable{
		@Override
		public void run() {
			Node t1Pos = list.get_head();
			while(q2.flag == true) {
				double random = Math.random() * 100;
				if(random <= 10 && random >= 0) {
					q2.list.delete(q2.list.get_Position(t1Pos));
					t1Pos = t1Pos.getNext();
				}
				try {
					Thread.sleep(20);
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static class T2 implements Runnable{
		@Override
		//Todo: add chance
		public void run() {
			Node t2Pos = list.get_head();
			while(q2.flag == true) {
				double random = Math.random() * 100;
				if(random >= 0 && random <= 10) {
					Node node = new Node(q2.randomChar());
					q2.list.add(node, q2.list.get_Position(t2Pos));
					t2Pos = t2Pos.getNext();
				}
				try {
					Thread.sleep(20);
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


}


