package pro.klimovich.planner

import android.app.Application
import pro.klimovich.planner.data.PlannerDatabase
import pro.klimovich.planner.data.PlannerRepository

class PlannerApplication : Application() {
    val database by lazy { PlannerDatabase.create(this) }
    val repository by lazy { PlannerRepository(database.plannerDao()) }
}
