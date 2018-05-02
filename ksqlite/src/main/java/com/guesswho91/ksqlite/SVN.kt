package com.guesswho91.ksqlite

/**
 * SaveNameValue
 * Using this annotation to save name of constructor fields
 * Is there any more element way???
 * Created by Leo on 30.04.2018.
 */

@Retention(AnnotationRetention.RUNTIME) annotation class SVN(val expression: String)