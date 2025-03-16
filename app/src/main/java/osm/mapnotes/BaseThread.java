package osm.mapnotes;

import android.os.Handler;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseThread extends Thread
{
  // Handler to send messages to the listener
  private Handler mHandler = null;

  // Input event semaphore
  private final Semaphore mInputEventSemaphore = new Semaphore(0);

  // Input lock
  private final ReentrantLock mInputEventLock = new ReentrantLock();

  // Abstract class ThreadEvent
  abstract public static class ThreadEvent
  {
  }

  // Input event queue
  private final ArrayList<ThreadEvent> mInputEventQueue = new ArrayList<>();

  private final ReentrantLock mOutputEventLock = new ReentrantLock();

  // Output event queue
  private final ArrayList<ThreadEvent> mOutputEventQueue = new ArrayList<>();

  // run() function to be instanced by subclasses
  public abstract void run();

  // dispatchEvent() function to be instanced by subclasses
  protected abstract void dispatchEvent(ThreadEvent event);

  public BaseThread()
  {
  }

  protected void createHandler()
  {
    mOutputEventLock.lock();

    mHandler = new Handler();

    dispatchOutputEvents();

    mOutputEventLock.unlock();
  }

  protected void addInputEvent(ThreadEvent threadEvent)
  {
    mInputEventLock.lock();

     mInputEventQueue.add(threadEvent);

    mInputEventLock.unlock();

    mInputEventSemaphore.release();
  }

  protected ThreadEvent waitForInputEvent() throws InterruptedException
  {
    mInputEventSemaphore.acquire();

    mInputEventLock.lock();

    ThreadEvent event = mInputEventQueue.remove(0);

    mInputEventLock.unlock();

    return event;
  }

  protected void addOutputEvent(ThreadEvent threadEvent)
  {
    mOutputEventLock.lock();

    mOutputEventQueue.add(threadEvent);

    dispatchOutputEvents();

    mOutputEventLock.unlock();
  }

  private void dispatchOutputEvents()
  {
    if (mHandler == null)
    {
      // No handler is set
      return;
    }

    while (!mOutputEventQueue.isEmpty())
    {
      final ThreadEvent event = mOutputEventQueue.remove(0);

      mHandler.post(() -> dispatchEvent(event));
    }
  }
}
