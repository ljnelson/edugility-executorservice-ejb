/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
 *
 * Copyright (c) 2012-2012 Edugility LLC.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * The original copy of this license is available at
 * http://www.opensource.org/license/mit-license.html.
 */
package com.edugility.executorservice.ejb;

import java.io.PrintStream;

import java.security.Principal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.ejb.embeddable.EJBContainer;

import javax.naming.Context;
import javax.naming.NamingException;

import com.edugility.callables.JNDICallable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A <a href="http://www.junit.org/">JUnit</a> test suite that
 * exercises the functionality of the {@link ExecutorServiceBean}
 * class.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 */
public class TestCaseExecutorServiceBean {

  /**
   * The {@link EJBContainer} used to run the test.  May be {@code
   * null} at any point.
   */
  private EJBContainer container;

  /**
   * The {@link Context} providing access to the EJB component
   * environment.  May be {@code null} at any point.
   */
  private Context context;

  /**
   * The {@link ExecutorServiceBean} under test.  This field may be
   * {@code null} at any point.
   */
  private ExecutorService bean;

  /**
   * A {@link Callable} to execute.  May be {@code null} at any point.
   */
  private Callable<Integer> callable;

  /**
   * A {@link Callable} that is set to a {@link JNDICallable}
   * instances.  May be {@code null} at any point.
   */
  private Callable<Principal> jndiCallable;
  
  /**
   * A {@link Runnable} for testing; may be {@code null} at any point.
   */
  private Runnable runnable;

  /**
   * Sets up the various test fixtures.
   *
   * @exception Exception if an error occurs during setup
   */
  @Before
  public void setUp() throws NamingException {
    assertNull(this.callable);
    this.callable = new Callable<Integer>() {
      @Override
      public final Integer call() {
        final String threadName = Thread.currentThread().getName();
        assertNotNull(threadName);
        assertTrue(threadName.startsWith("__ejb-thread-pool"));
        return Integer.valueOf(1);
      }
    };

    this.jndiCallable = new JNDICallable<Principal>("java:comp/EJBContext", javax.ejb.EJBContext.class, "getCallerPrincipal") {
      private static final long serialVersionUID = 1L;
      @Override
      public final Principal call() throws Exception {
        final String threadName = Thread.currentThread().getName();
        assertNotNull(threadName);
        assertTrue(threadName.startsWith("__ejb-thread-pool"));
        return super.call();
      }
    };
    
    assertNull(this.runnable);
    this.runnable = new Runnable() {
        @Override
        public final void run() {
          final String threadName = Thread.currentThread().getName();
          assertNotNull(threadName);
          assertTrue(threadName.startsWith("__ejb-thread-pool"));
        }
      };
    assertNull(this.bean);
    assertNull(this.context);
    assertNull(this.container);
    this.container = EJBContainer.createEJBContainer();
    assertNotNull(this.container);
    this.context = this.container.getContext();
    assertNotNull(this.context);
    final Object rawBean = this.context.lookup("java:global/classes/ExecutorServiceBean!java.util.concurrent.ExecutorService");
    assertTrue(rawBean instanceof ExecutorService);
    this.bean = (ExecutorService)rawBean;
  }

  /**
   * Tears down the various test fixtures.
   *
   * @exception Exception if an error occurs during the dismantling of
   * the {@link EJBContainer} or for some other reason
   */
  @After
  public void tearDown() throws NamingException {
    this.runnable = null;
    this.callable = null;
    this.bean = null;
    assertNotNull(this.container);
    assertNotNull(this.context);
    this.context.close();
    this.context = null;
    this.container.close();
    this.container = null;
  }
  
  /**
   * Tests the {@link ExecutorServiceBean#submit(Callable)} method.
   */
  @Test
  public void testSubmit() throws ExecutionException, InterruptedException {
    final Future<Integer> result = this.bean.submit(this.callable);
    assertNotNull(result);
    assertEquals(Integer.valueOf(1), result.get());
  }

  /**
   * Tests the actual operation of a {@link JNDICallable} instance.
   */
  @Test
  public void testSubmitJNDICallable() throws ExecutionException, InterruptedException {
    final Future<Principal> result = this.bean.submit(this.jndiCallable);
    assertNotNull(result);
    final Principal userPrincipal = result.get();
    assertNotNull(userPrincipal);
    assertEquals("ANONYMOUS", userPrincipal.getName());
  }

  /**
   * Tests the {@link ExecutorServiceBean#execute(Runnable)} method.
   */
  @Test
  public void testExecute() {
    this.bean.execute(this.runnable);
  }

}