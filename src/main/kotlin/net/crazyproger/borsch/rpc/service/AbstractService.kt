package net.crazyproger.borsch.rpc.service

import org.jetbrains.exposed.sql.Database

open class AbstractService(protected val database: Database)