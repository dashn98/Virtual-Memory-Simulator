import java.io.*;
import java.util.*;
class vmsim
{
  public static void main(String[] args)
  {
    int num_frames = 0;
    String argument = "";
    int refresh = 0;;
    String file = "";
    for(int i=0;i< args.length;i++)
    {
      if(args[i].equals("-n"))
      {
        num_frames = Integer.parseInt(args[i+1]);
      }
      else if(args[i].equals("-a"))
      {
        argument = args[i+1];
      }
      else if(args[i].equals("-r"))
      {
        refresh = Integer.parseInt(args[i+1]);
      }
    }

    if(args[args.length-1] != null)
    {
      file = args[args.length-1];
    }

    if(argument.equals("opt"))
    {
      opt(num_frames, file);
    }
    else if(argument.equals("fifo"))
    {
      try
      {
        fifo(num_frames, file);
      }
      catch (FileNotFoundException ex)
      {
        System.out.println("File not found");
      }
    }
    else if(argument.equals("aging"))
    {
      aging(num_frames, refresh, file);
    }
    else
    {
      System.out.println("Invalid algorithm");
    }
  }

  public static void fifo(int num_frames, String file) throws FileNotFoundException
  {
    Queue<Page> page_queue = new LinkedList<Page>(); // This can never be larger in size than num_frames
    int mem_access = 0;
    int page_faults = 0;
    int writes = 0;
    try
    {
      Scanner reader = new Scanner(new File(file));
      while (reader.hasNext())
      {
        mem_access++;
        Page new_page = new Page();
        new_page.set_instruction(reader.next().charAt(0));
        String address = reader.next();
        int index = Integer.parseInt(address.substring(2,7),16);
        new_page.set_index(index);
        reader.next();
        if(new_page.get_instruction() == 's')
        {
          new_page.set_dirty_bit(true);
        }
        else
        {
          new_page.set_dirty_bit(false);
        }
        if(page_queue.size() < num_frames)
        {
          if(in_queue(new_page.get_index(), page_queue))
          {
            if(new_page.get_instruction() == 's')
            {
              // set dirty bit
              for(Page item: page_queue)
              {
                if(item.get_index() == new_page.get_index())
                {
                  item.set_dirty_bit(true);
                }
              }
            }
          }
          else
          {
            page_queue.add(new_page);
            page_faults++;
          }
        }
        else if(in_queue(new_page.get_index(), page_queue))
        {
          if(new_page.get_instruction() == 's')
          {
            // set dirty bit
            for(Page item: page_queue)
            {
              if(item.get_index() == new_page.get_index())
              {
                item.set_dirty_bit(true);
              }
            }
          }
        }
        else
        {

          Page p = page_queue.remove();
          if(p.get_dirty_bit() == true)
          {
            writes++;
          }
          page_queue.add(new_page);
          page_faults++;

        }
      }
      reader.close();
    }
    catch (FileNotFoundException ex)
    {
        System.out.println("File not found");
    }

      System.out.println("Algorithm: FIFO");
      System.out.println("Number of frames: " + num_frames);
      System.out.println("Total memory accesses: " + mem_access);
      System.out.println("Total page faults: " + page_faults);
      System.out.println("Total writes to disk: " + writes);
  }

  public static boolean in_queue(int index, Queue<Page> page_queue)
  {
    for(Page item: page_queue)
    {
      if(item.get_index() == index)
      {
        return true;
      }
    }
    return false;
  }

  public static void opt(int num_frames, String file)
  {
    ArrayList<Page> page_arr = new ArrayList<Page>(); // This can never be larger in size than num_frames
    ArrayList<Page> all_pages = new ArrayList<Page>(); // This holds all all pages
    HashMap<Integer,ArrayList<Integer>> cycles =new HashMap<Integer,ArrayList<Integer>>();
    int mem_access = 0;
    int page_faults = 0;
    int writes = 0;
    try
    {
      Scanner reader = new Scanner(new File(file));
      while (reader.hasNext())
      {
        mem_access++;
        Page new_page = new Page();
        // set the instruction to the char read in from the file
        new_page.set_instruction(reader.next().charAt(0));
        // set address as the next string read from file
        String address = reader.next();
        // parse address to integer
        int index = Integer.parseInt(address.substring(2,7),16);
        // set the page's index to address
        new_page.set_index(index);
        reader.next();
        // if hashmap does not have an arraylist at the current page's address put an arraylist at the key value where key value is page's address
        if(cycles.get(index) == null)
        {
          cycles.put(index, new ArrayList<Integer>());
        }
        // add the memory access number to the arraylist at the page address key arraylist in the hashmap
        cycles.get(index).add(mem_access);
        // set the mem_access number for the new page
        new_page.set_cycles(mem_access);
        // add page to arraylist that holds all traces in the trace file
        all_pages.add(new_page);
      }
      reader.close();
    }
    catch (FileNotFoundException ex)
    {
        System.out.println("File not found");
    }

    // loop through all_pages array which represents all traces from the file in page format inside an arraylist
    for(int i = 0; i < all_pages.size(); i++)
    {
      // remove the top of the arraylist in the hashmap at current trace's address
      cycles.get(all_pages.get(i).get_index()).remove(0);

      // check if the current page is already in the list of frames
      if(in_list(page_arr, all_pages.get(i).get_index()))
      {
        // if the current page has an s instruction we need to change the page in the page fram array to to be dirty
        if(all_pages.get(i).get_instruction() == 's')
        {
          // find the array that has the same address as the current page
          for(int m = 0; m < page_arr.size(); m++)
          {
            if(page_arr.get(m).get_index() == all_pages.get(i).get_index())
            {
              // set the instruction to s so when evicted writes will be incremented
              page_arr.get(m).set_instruction('s');
            }
          }
        }
      }
      else if(page_arr.size() < num_frames)
      {
        // if the new page isn't in the frames array and frames array hasn't exceeded the size then add it to the page frames array
          page_arr.add(all_pages.get(i));
          page_faults++;
      }
      else // eviction
      {
        int max_cycles = -1;
        int max_index = -1;
        int empty_index = -1;
        ArrayList<Integer> cycles_list;
        for(int k = 0; k < page_arr.size(); k++)
        {
          // get the arraylist of the mem_access numbers at the page's address from hashmap
          cycles_list = cycles.get(page_arr.get(k).get_index());
          // if the cycles_list is empty then we want this one to be the one we evict - must check for ties though, if the cycles list isn't empty - if the top of this list is greater than max then update max
          if(cycles_list.isEmpty())
          {
            // if the empty index hasn't been set yet then set the empty index to current otherwise check for clean bit otherwise check for lowest address
            if(empty_index == -1)
            {
              empty_index = k;
            }
            else if(page_arr.get(empty_index).get_instruction() == 's' &&  page_arr.get(k).get_instruction() != 's')
            {
              empty_index = k;
            }
            else if(page_arr.get(k).get_index() < page_arr.get(empty_index).get_index() && page_arr.get(empty_index).get_instruction() ==  page_arr.get(k).get_instruction())
            {
              empty_index = k;
            }
          }
          else if(cycles_list.get(0) > max_cycles)
          {
            max_index = k;
            max_cycles = cycles_list.get(0);
          }
        }

        // If empty index is -1 then there were no page address's with that never showed up again in trace so we want to evict the one that occurs furthest next
        if(empty_index == -1)
        {
          // if the frame at the max index is a store then increment writes
          if(page_arr.get(max_index).get_instruction() == 's')
          {
            writes++;
          }
          // remove the page
          page_arr.remove(max_index);
          // add current page
          page_arr.add(all_pages.get(i));
          // increment page faults
          page_faults++;
        }
        else
        {
          // otherwise remove the empty_index page - first check if its a store and increment writes
          if(page_arr.get(empty_index).get_instruction() == 's')
          {
            writes++;
          }
          // remove page at empty_index
          page_arr.remove(empty_index);
          // add the current page
          page_arr.add(all_pages.get(i));
          // increment page_faults
          page_faults++;
        }
      }
    }
    System.out.println("Algorithm: OPT");
    System.out.println("Number of frames: " + num_frames);
    System.out.println("Total memory accesses: " + mem_access);
    System.out.println("Total page faults: " + page_faults);
    System.out.println("Total writes to disk: " + writes);
  }

  public static boolean in_list(ArrayList<Page> page_arr, int index)
  {
    //System.out.println("INDEX I'm searching for " + index);
    // loop through entire page frame array
    for(int i = 0; i < page_arr.size(); i++)
    {
      // if the page frame address matches the address we are looking at then return true
      if(page_arr.get(i).get_index() == index)
      {
        //System.out.println("Page index I found" + page_arr.get(i).get_index());
        return true;
      }
    }
    return false;
  }

  public static void aging(int num_frames, int refresh, String file)
  {
    ArrayList<Page> page_arr = new ArrayList<Page>(); // This can never be larger in size than num_frames
    int mem_access = 0;
    int page_faults = 0;
    int writes = 0;
    int refresh_count = 0;
    try
    {
      Scanner reader = new Scanner(new File(file));
      while (reader.hasNext())
      {
        // Create new page from line read in
        Page new_page = new Page();
        // set first char on line to instruction
        new_page.set_instruction(reader.next().charAt(0));
        // read in address string
        String address = reader.next();
        // parse address string to integer
        int index = Integer.parseInt(address.substring(2,7),16);
        // add address to the new_page
        new_page.set_index(index);
        // initialize reference bit to false
        new_page.set_reference_bit(false);
        // initialize counter bit to 1000 0000
        new_page.set_counter_bit(128);

        // increment refresh_count by the cycles that are read in
        refresh_count += Integer.parseInt(reader.next());

        // if it is not the first instruction increment refresh count by 1 for l or s
        if(mem_access > 0)
        {
          refresh_count++;
        }

        // while the refresh count is greater than the refresh amount refresh the pages
        while(refresh_count >= refresh)
        {
          page_arr = refresh_pages(page_arr);
          // decrement the refresh count
          refresh_count -= refresh;
        }

        // if the new page is already in rhe frame find that page in the frame array
        if(in_list(page_arr, new_page.get_index()))
        {
            for(int i = 0; i < page_arr.size(); i++)
            {
              if(page_arr.get(i).get_index() == new_page.get_index())
              {
                // set the reference bit to true
                page_arr.get(i).set_reference_bit(true);
                // check if the new page's instruction is s
                if(new_page.get_instruction() == 's')
                {
                  // set the page_arr instruction to s so it will increment writes when it is evicted
                  page_arr.get(i).set_instruction('s');
                }
              }
            }
        }
        else if(page_arr.size() < num_frames)
        {
            // if the page frame array is not yet full we add the new page
            page_arr.add(new_page);
            // increment page faults
            page_faults++;
        }
        else // Eviction
        {
          // set the lowest counter to the first item's counter bit
          int lowest_counter = page_arr.get(0).get_counter_bit();

          // loop through page fram array and find the lowest counter bit
          for(int j = 0; j < page_arr.size(); j++)
          {
            if(page_arr.get(j).get_counter_bit() < lowest_counter)
            {
              lowest_counter  = page_arr.get(j).get_counter_bit();
            }
          }

          // check for ties
          int lowest_index = -1; // set the lowest_index to -1
          for(int k = 0; k < page_arr.size(); k++)
          {
            if(page_arr.get(k).get_counter_bit() == lowest_counter)
            {
              if(lowest_index == -1)
              {
                lowest_index = k; // the first index in the page frame array to have the lowest counter value sets the lowest_index
              }
              else if(page_arr.get(lowest_index).get_instruction() == 's' && page_arr.get(k).get_instruction() != 's')
              {
                lowest_index = k; // if the current page is clean and previous is dirty then reset lowest_index to current page
              }
              else if(page_arr.get(k).get_index() < page_arr.get(lowest_index).get_index() && page_arr.get(lowest_index).get_instruction() == page_arr.get(k).get_instruction())
              {
                lowest_index = k; // if the current and previous page both are clean or both are dirty then set the lowest index to whatever has the lowest  page address
              }
            }
          }

          // if the page frame array at lowest index has an s for the instruction then increment writes
          if(page_arr.get(lowest_index).get_instruction() == 's')
          {
            writes++;
          }
          // remove page at lowest index
          page_arr.remove(lowest_index);
          // add the new page
          page_arr.add(new_page);
          // increment page faults
          page_faults++;
        }
        // incrememnt mem_access for every iteration of while loop
        mem_access++;
      }
      reader.close();
    }
    catch (FileNotFoundException ex)
    {
        System.out.println("File not found");
    }
      System.out.println("Algorithm: AGING");
      System.out.println("Number of frames: " + num_frames);
      System.out.println("Total memory accesses: " + mem_access);
      System.out.println("Total page faults: " + page_faults);
      System.out.println("Total writes to disk: " + writes);
  }

  public static ArrayList<Page> refresh_pages(ArrayList<Page> page_arr)
  {
    // for every page in the page frame array shift the counter bit right by one
    for(int i = 0; i < page_arr.size(); i++)
    {
      page_arr.get(i).shift_counter_bit();
      // if the reference bit has been sit then set the leftmost counter bit to 1
      if(page_arr.get(i).get_reference_bit() == true)
      {
        page_arr.get(i).leftmost_counter_bit();
        // reset the reference bit to false
        page_arr.get(i).set_reference_bit(false);
      }
    }
    return page_arr;
  }
}
