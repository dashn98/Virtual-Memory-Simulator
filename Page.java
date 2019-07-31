import java.util.*;
public class Page
{
  private int index;
  private char instruction;
  private boolean dirty_bit;
  private boolean reference_bit;
  private int counter_bit;
  private int cycles;

  public Page()
  {
    dirty_bit = false;
    //reference_bit = false;
  }
  public Page(int new_index)
  {
    index = new_index;
    dirty_bit = false;
  //  reference_bit = false;
  }

  public int get_index()
  {
    return index;
  }

  public void set_index(int new_index)
  {
    index = new_index;
  }

  public char get_instruction()
  {
    return instruction;
  }

  public void set_instruction(char new_instruction)
  {
    instruction = new_instruction;
  }

  public boolean get_dirty_bit()
  {
    return dirty_bit;
  }

  public void set_dirty_bit(boolean new_dirty_bit)
  {
    dirty_bit = new_dirty_bit;
  }

  public boolean get_reference_bit()
  {
    return reference_bit;
  }

  public void set_reference_bit(boolean new_reference_bit)
  {
    reference_bit = new_reference_bit;
  }

  public int get_cycles()
  {
    return cycles;
  }

  public void set_cycles(int new_cycles)
  {
    cycles = new_cycles;
  }

  public int get_counter_bit()
  {
    return counter_bit;
  }

  public void set_counter_bit(int new_counter_bit)
  {
    counter_bit = new_counter_bit;
  }

  public void shift_counter_bit()
  {
    //System.out.println("COUNTER BIT BEFORE " + counter_bit);
    counter_bit = counter_bit >> 1;
    //System.out.println("COUNTER BIT AFTER " + counter_bit);
  }

  public void leftmost_counter_bit()
  {
  //  System.out.println("COUNTER BIT BEFORE " + counter_bit);
    counter_bit = counter_bit | 128;
  //  System.out.println("COUNTER BIT AFTER " + counter_bit);
  }
}
