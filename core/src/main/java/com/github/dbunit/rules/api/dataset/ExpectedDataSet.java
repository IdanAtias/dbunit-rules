package com.github.dbunit.rules.api.dataset;

import com.github.dbunit.rules.dataset.DataSetExecutorImpl;

import java.lang.annotation.*;

/**
 * Created by rafael-pestano on 22/07/2015.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ExpectedDataSet {

  /**
   * @return dataset file name using resources folder as root directory
   */
  String value();

  /**
   *
   * @return column names to ignore in comparison
   */
  String[] ignoreCols() default "";
}